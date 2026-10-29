SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia" AS
SELECT
	 TE."IdTarjetaExistencia",
	 TE."IdEmpresa",
	 TE."FechaProceso",
	 TE."FechaSistema",
	 TE."IdCliente",
	 CC."NombreCompleto" AS "NombreCliente",
	 CC."DocumentoIdentidad",
	 CC."DocumentoIdentidadAdicional",
	 TE."IdPortafolio",
	 CP."CodigoPortafolio",
	 CP."DescripcionPortafolio",
	 CP."Proposito",
	 TE."FechaOperacion",
	 TE."Tipo",
	 CASE WHEN TE."Tipo" = 'INGRESO' THEN 'COMPRA' ELSE 'VENTA' END "TipoUI",
	 TE."FolioCartera",
	 TE."IdConcepto",
	 CPT."Nombre" AS "NombreConcepto",
	 TE."IdClasificacionTipoInstrumento",
	 TE."IdTipoInstrumento",
	 TE."IdInstrumento",
	 INS."Nemotecnico",
	 INS."ISIN",
	 INS."IdTipoReajuste",
	 INS."IdEmisor",
	 TE."IdMoneda",
	 TE."CantidadIngreso",
	 TE."CantidadEgreso",
	 TE."SaldoCantidad",
	 TE."PrecioAdquisicion",
	 TE."MontoIngreso",
	 TE."MontoEgreso",
	 TE."SaldoMontoPrecioAdquisicion",
	 TE."PrecioPromedioPonderado",
	 TE."MontoAPrecioPromedioPonderado",
	 TE."SaldoMontoAPrecioPromedioPonderado",
	 TE."Perdida",
	 TE."Ganancia",
	 TE."Acumulado",
	 TE."UltimoIngreso",
	 TE."UltimoRegistro",
	 TE."RegistroDeArrastre",
	 TE."IdEstado" 
FROM "BSCL.Custodia.BD::CUS.TarjetaExistencia" AS TE 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = TE."IdCliente" 
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON CP."IdPortafolio" = TE."IdPortafolio" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON CPT."IdConcepto" = TE."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = TE."IdInstrumento" 
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO