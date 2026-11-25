"""Convierte el Markdown del informe de Discovery en un PDF A4 apaisado."""

import argparse
from html import escape
from pathlib import Path
import re

import reportlab
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import CondPageBreak, LongTable, PageBreak, Paragraph, SimpleDocTemplate, Spacer


PAGE = landscape(A4)
MARGIN = 39
INK = colors.HexColor("#172332")
MUTED = colors.HexColor("#4E5D6B")
RULE = colors.HexColor("#CCD4DD")
PALE = colors.HexColor("#EEF2F5")
LINK = "#185C80"
HEADING = re.compile(r"^(#{1,6})\s+(.+)")
LIST_ITEM = re.compile(r"^(\s*)([-+*]|\d+\.)\s+(.+)")
PAGE_BREAK = "<!-- salto-de-pagina -->"
COVER_OFFSET = PAGE[1] * 0.22
SEPARATOR = re.compile(r"^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)*\|?\s*$")
INLINE = re.compile(
    r"`([^`]+)`|\[([^\]]+)\]\((https?://[^\s()]+(?:\([^\s()]*\)[^\s()]*)*)\)"
    r"|<(https?://[^\s<>]+)>|(https?://[^\s<>]+)"
    r"|\*\*(.+?)\*\*|__(.+?)__|(?<!\w)\*([^*]+)\*|(?<!\w)_([^_]+)_(?!\w)"
    r"|<br\s*/?>|\\([\\|*_])",
    re.IGNORECASE,
)


def register_fonts():
    system = Path("/System/Library/Fonts/Supplemental")
    bundled = Path(reportlab.__file__).parent / "fonts"
    arial = ["Arial.ttf", "Arial Bold.ttf", "Arial Italic.ttf", "Arial Bold Italic.ttf"]
    vera = ["Vera.ttf", "VeraBd.ttf", "VeraIt.ttf", "VeraBI.ttf"]
    if all((system / name).is_file() for name in arial):
        directory, filenames = system, arial
    else:
        directory, filenames = bundled, vera
    normal, bold, italic, bold_italic = ("Text", "Text-Bold", "Text-Italic", "Text-BoldItalic")
    for name, filename in zip((normal, bold, italic, bold_italic), filenames):
        pdfmetrics.registerFont(TTFont(name, directory / filename))
    pdfmetrics.registerFontFamily(normal, normal=normal, bold=bold, italic=italic, boldItalic=bold_italic)


def hyperlink(url, label):
    return f'<link href="{escape(url, quote=True)}" color="{LINK}"><u>{label}</u></link>'


def inline(text, *, allow_links=True):
    """Traduce el formato en línea a etiquetas seguras para ReportLab."""
    parts = []
    cursor = 0
    for match in INLINE.finditer(text):
        parts.append(escape(text[cursor:match.start()]))
        code, label, target, autolink, url, strong, alt_strong, emphasis, alt_emphasis, literal = match.groups()
        if code is not None:
            parts.append(escape(code))
        elif label is not None and allow_links:
            parts.append(hyperlink(target, inline(label, allow_links=False)))
        elif autolink is not None and allow_links:
            parts.append(hyperlink(autolink, escape(autolink)))
        elif url is not None and allow_links:
            address = url.rstrip(".,;:!?")
            while address.endswith(")") and address.count(")") > address.count("("):
                address = address[:-1]
            parts.append(hyperlink(address, escape(address)) + escape(url[len(address):]))
        elif strong is not None or alt_strong is not None:
            parts.append(f"<b>{inline(strong or alt_strong, allow_links=allow_links)}</b>")
        elif emphasis is not None or alt_emphasis is not None:
            parts.append(f"<i>{inline(emphasis or alt_emphasis, allow_links=allow_links)}</i>")
        elif literal is not None:
            parts.append(escape(literal))
        elif match.group().lower().startswith("<br"):
            parts.append("<br/>")
        else:
            parts.append(escape(match.group()))
        cursor = match.end()
    parts.append(escape(text[cursor:]))
    return "".join(parts)


def make_styles():
    body = ParagraphStyle(
        "Body", fontName="Text", fontSize=10.5, leading=15, textColor=INK,
        alignment=TA_LEFT, spaceAfter=7, splitLongWords=True,
    )
    return {
        "body": body,
        "title": ParagraphStyle(
            "Title", parent=body, fontName="Text-Bold", fontSize=25, leading=30,
            spaceAfter=15, keepWithNext=True,
        ),
        "section": ParagraphStyle(
            "Section", parent=body, fontName="Text-Bold", fontSize=16, leading=20,
            spaceBefore=13, spaceAfter=6, keepWithNext=True,
        ),
        "subsection": ParagraphStyle(
            "Subsection", parent=body, fontName="Text-Bold", fontSize=12, leading=16,
            spaceBefore=9, spaceAfter=5, keepWithNext=True,
        ),
        "cell": ParagraphStyle("Cell", parent=body, fontSize=9.2, leading=12, spaceAfter=0),
        "header": ParagraphStyle(
            "Header", parent=body, fontName="Text-Bold", fontSize=9.2, leading=12, spaceAfter=0,
        ),
    }


def table_cells(line):
    line = line.strip().removeprefix("|")
    if line.endswith("|") and not line.endswith(r"\|"):
        line = line[:-1]
    return [cell.strip() for cell in re.split(r"(?<!\\)\|", line)]


def make_table(rows, width, styles):
    columns = len(rows[0])
    cells = [
        [Paragraph(inline(value), styles["header" if index == 0 else "cell"]) for value in row]
        for index, row in enumerate(rows)
    ]
    weights = []
    minimums = []
    for column in range(columns):
        heading = cells[0][column].getPlainText()
        header = pdfmetrics.stringWidth(heading, "Text-Bold", 9.2)
        longest_word = max(
            (pdfmetrics.stringWidth(word, "Text-Bold", 9.2) for word in heading.split()),
            default=0,
        )
        body = sorted(
            pdfmetrics.stringWidth(row[column].getPlainText(), "Text", 9.2)
            for row in cells[1:]
        )
        typical = body[len(body) // 2] if body else 0
        minimums.append(max(62, min(90, longest_word + 13)))
        weights.append(max(52, min(240, max(header, typical))))
    base_width = sum(minimums)
    if base_width > width:
        minimums = [value * width / base_width for value in minimums]
    flexible = max(0, width - base_width)
    total = sum(weights)
    column_widths = [minimum + flexible * weight / total for minimum, weight in zip(minimums, weights)]
    row_heights = [
        max(cell.wrap(cell_width - 12, PAGE[1])[1] for cell, cell_width in zip(row, column_widths)) + 12
        for row in cells
    ]
    usable_height = PAGE[1] - 37 - 44 - 12
    split_rows = bool(row_heights[1:]) and row_heights[0] + max(row_heights[1:]) > usable_height
    return LongTable(
        cells, colWidths=column_widths, repeatRows=1, splitInRow=split_rows,
        hAlign="LEFT", spaceBefore=4, spaceAfter=9,
        style=[
            ("BACKGROUND", (0, 0), (-1, 0), PALE),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 6),
            ("RIGHTPADDING", (0, 0), (-1, -1), 6),
            ("TOPPADDING", (0, 0), (-1, -1), 6),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ("GRID", (0, 0), (-1, -1), 0.35, RULE),
            ("LINEBELOW", (0, 0), (-1, 0), 0.65, MUTED),
        ],
    )


def markdown_story(markdown, width, styles):
    lines = markdown.splitlines()
    story = []
    pending = []
    title = None

    def flush_paragraph():
        if pending:
            story.append(Paragraph(inline(" ".join(pending)), styles["body"]))
            pending.clear()

    index = 0
    while index < len(lines):
        raw = lines[index]
        line = raw.strip()
        heading = HEADING.fullmatch(line)
        item = LIST_ITEM.fullmatch(raw)
        if not line:
            flush_paragraph()
        elif line == PAGE_BREAK:
            # El primer salto cierra la portada: su contenido baja hacia el centro.
            flush_paragraph()
            if not any(isinstance(flowable, PageBreak) for flowable in story):
                story.insert(0, Spacer(1, COVER_OFFSET))
            story.append(PageBreak())
        elif heading:
            flush_paragraph()
            depth, text = heading.groups()
            style = "subsection"
            if len(depth) == 1 and title is None:
                style = "title"
            elif len(depth) <= 2:
                style = "section"
            paragraph = Paragraph(inline(text), styles[style])
            if style == "title":
                title = paragraph.getPlainText()
            story.append(paragraph)
        elif index + 1 < len(lines) and "|" in line and SEPARATOR.fullmatch(lines[index + 1]):
            flush_paragraph()
            if story and story[-1].getKeepWithNext():
                # Agrupar el título con toda la tabla desperdicia espacio al paginar.
                first_heading = len(story) - 1
                while first_heading > 0 and story[first_heading - 1].getKeepWithNext():
                    first_heading -= 1
                heading_space = sum(
                    heading.wrap(width, PAGE[1])[1] + heading.getSpaceBefore() + heading.getSpaceAfter()
                    for heading in story[first_heading:]
                )
                story[-1].keepWithNext = False
                story.insert(first_heading, CondPageBreak(heading_space + 70))
            rows = [table_cells(line)]
            index += 2
            while index < len(lines) and "|" in lines[index]:
                row = table_cells(lines[index])
                if len(row) != len(rows[0]):
                    break
                rows.append(row)
                index += 1
            story.append(make_table(rows, width, styles))
            continue
        elif item:
            flush_paragraph()
            indent, marker, text = item.groups()
            offset = 13 * (len(indent.expandtabs(4)) // 2)
            style = ParagraphStyle(
                "List item", parent=styles["body"], leftIndent=offset + 16,
                bulletIndent=offset, bulletFontName="Text", spaceAfter=3,
            )
            bullet = marker if marker.endswith(".") else "•"
            story.append(Paragraph(inline(text), style, bulletText=bullet))
        elif re.fullmatch(r"(?:-\s*){3,}|(?:\*\s*){3,}|(?:_\s*){3,}", line):
            flush_paragraph()
        else:
            pending.append(line)
        index += 1
    flush_paragraph()
    return story, title


def draw_footer(canvas, document):
    canvas.saveState()
    canvas.setStrokeColor(RULE)
    canvas.setLineWidth(0.5)
    canvas.line(MARGIN, 33, PAGE[0] - MARGIN, 33)
    canvas.setFillColor(MUTED)
    canvas.setFont("Text", 8.5)
    canvas.drawRightString(PAGE[0] - MARGIN, 19, f"Página {document.page}")
    canvas.restoreState()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("markdown", type=Path, help="Ruta del informe Markdown")
    parser.add_argument("pdf", type=Path, help="Ruta del PDF a generar")
    args = parser.parse_args()
    if args.markdown.resolve() == args.pdf.resolve():
        parser.error("Las rutas de entrada y salida deben ser distintas.")
    register_fonts()
    document = SimpleDocTemplate(
        str(args.pdf), pagesize=PAGE, leftMargin=MARGIN, rightMargin=MARGIN,
        topMargin=37, bottomMargin=44, lang="es", invariant=1,
    )
    story, title = markdown_story(args.markdown.read_text(encoding="utf-8-sig"), document.width - 12, make_styles())
    document.title = title or args.markdown.stem
    document.build(story, onFirstPage=draw_footer, onLaterPages=draw_footer)


if __name__ == "__main__":
    main()
