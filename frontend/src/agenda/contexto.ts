import type { Documento } from "../dom/nodo.js";
import type { Profesional } from "../api/types.js";
import type { Ruta } from "./ruta.js";
import type { Bloqueo, PacienteElegible, Prestacion, ResultadoEdicion, Rol, Sillon, TurnoActualizado, TurnoAgenda } from "./tipos.js";

export interface AltaTurno {
  pacienteId: string;
  profesionalId: string;
  sillonId: string;
  prestacionId: string;
  inicio: string;
  duracionAjustadaMinutos: number;
}

export interface AltaBloqueo {
  sillonId: string | null;
  profesionalId: string | null;
  inicio: string;
  fin: string;
}

export interface ReprogramacionTurno {
  versionEsperada: string;
  profesionalId: string;
  sillonId: string;
  inicio: string;
}

export interface ApiAgenda {
  sillones(): Promise<Sillon[]>;
  profesionales(): Promise<Profesional[]>;
  prestaciones(): Promise<Prestacion[]>;
  intervalo(): Promise<number | null>;
  establecerIntervalo(minutos: number): Promise<number | null>;
  turnos(sillonId: string, desde: string, hasta: string): Promise<TurnoAgenda[]>;
  bloqueos(sillonId: string, desde: string, hasta: string): Promise<Bloqueo[]>;
  crearTurno(solicitud: AltaTurno): Promise<void>;
  reprogramarTurno(turnoId: string, solicitud: ReprogramacionTurno): Promise<TurnoActualizado>;
  cancelarTurno(turnoId: string, versionEsperada: string): Promise<TurnoActualizado>;
  crearBloqueo(solicitud: AltaBloqueo): Promise<void>;
  buscarPacientes(texto: string): Promise<PacienteElegible[]>;
}

/** Lo que necesita una vista; `vigente` descarta respuestas de una navegación anterior. */
export interface Contexto {
  doc: Documento;
  raiz: HTMLElement;
  api: ApiAgenda;
  usuario: { rol: Rol; profesionalRef: string | null };
  hoy: string;
  /** Identificador del control que tenía el foco antes de volver a dibujar. */
  foco: string | null;
  navegar(ruta: Ruta): void;
  reemplazar(ruta: Ruta): void;
  vigente(): boolean;
  conservarEdicion?(resultado: ResultadoEdicion): void;
  /** Inicia un envío; devuelve null si otro con la misma clave sigue en curso, aunque se haya cambiado de vista. */
  enviar(clave: string, operacion: () => Promise<void>, traducirFuera?: (error: unknown) => string): Promise<void> | null;
}
