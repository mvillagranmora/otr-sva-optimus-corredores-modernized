INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'NO_SE_INGRESARON_SOLICITUDES_RESCATE', 'No se ingresaron solicitudes de rescate');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'VALOR_CUOTA_INCORRECTO', 'Valor cuota incorrecto');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'MONTO_VALIDO', 'Monto invalido');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'FONDO_BLOQUEADO_REQ_APO_RES', 'Fondo ::IdInstrumento:: bloqueado ::DocumentoIdentidad::');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'FONDO_NO_DIFERIDO_REQ_APO_RES', 'Fondo no diferido ::DocumentoIdentidad::');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo", "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion", "TieneCarga", "TieneIngresoManual", "TieneCertificacion", "IdEstado", "GeneraOmnibus") VALUES ('RESCNN', 'RESCNN', 'RES', 'RESCFM', '', '', '', 'ACT', '');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente", "IdPersona", "FechaCreacion", "IdPersonaEjecutivo") VALUES (103, 1000, '2020-01-01 00:00:00', 9999);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado" ("IdCliente", "IdEstado") VALUES (103, 'ACT');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "CodigoPortafolio", "IdCliente", "IdEstado", "Proposito") VALUES (503, 'PF503', 103, 'ACT', 'PROPGEN');
