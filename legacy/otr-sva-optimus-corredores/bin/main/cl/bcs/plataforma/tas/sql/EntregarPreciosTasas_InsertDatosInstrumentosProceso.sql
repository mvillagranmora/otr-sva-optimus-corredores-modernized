/**************************************************************** 
Proposito	: Inserta registros en tabla global de proceso
autor		: JVILLAGRAN - BCS
fecha		: 04/07/2018
*****************************************************************/

INSERT INTO "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso"("IdRegistro","TipoCartera","IdUso","IdTipoCriterioTasa","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMoneda","FechaConsulta","PlazoResidual")
VALUES (
	:IdRegistro,
	:TipoCartera,
	:IdUso,
	:IdTipoCriterioTasa,
	:IdClasificacionTipoInstrumento,
	:IdTipoInstrumento,
	:IdInstrumento,
	:Nemotecnico,
	:IdMoneda,
	:FechaConsulta,
	:PlazoResidual
);