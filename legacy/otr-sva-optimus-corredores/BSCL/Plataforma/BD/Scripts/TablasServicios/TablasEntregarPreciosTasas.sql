SET SCHEMA "BSCL";

--entrada
DROP TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales";
DROP TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos";
--proceso

--salida
DROP TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos";
DROP TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle"; 


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales" (
	 "IdEmpresa"	BIGINT
	,"IdUsuario"	BIGINT
    ,"IdModulo"		NVARCHAR(3)
    ,"IdPais"		BIGINT
    ,"IdTrabajo"	BIGINT
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos" (
	 "IdRegistro"						BIGINT
	,"IdTrabajo"						BIGINT
	,"TipoCartera"						NVARCHAR(250)
	,"IdUso"							NVARCHAR(40)
	,"IdTipoCriterioTasa"				NVARCHAR(40)
	,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
	,"IdTipoInstrumento"				NVARCHAR(3)
	,"IdInstrumento"					BIGINT
	,"Nemotecnico"						NVARCHAR(40)
	,"IdMoneda"							NVARCHAR(3)
	,"FechaConsulta"					DATE
	,"PlazoResidual" 					BIGINT
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos" (
	 "IdRegistro"						BIGINT
	,"IdTrabajo"						BIGINT
	,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
	,"IdTipoInstrumento"				NVARCHAR(3)
	,"IdInstrumento"					BIGINT
	,"Nemotecnico"						NVARCHAR(40)
	,"IdTipoTasa"						NVARCHAR(40)
	,"Tasa"								DECIMAL(26,8)
	,"IdTipoPrecio"						NVARCHAR(40)
	,"Precio"							DECIMAL(26,8)
	,"CodigoFuente"						NVARCHAR(40)
	,"MensajeFuente"					NVARCHAR(250)
	,"FechaFuente"						SECONDDATE
	,"MensajeError"						NVARCHAR(250)
	,"IdMoneda"							NVARCHAR(3)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle"(
	 "Codigo" 		NVARCHAR(100)
	,"Descripcion" 	NVARCHAR(520)
	,"IdTrabajo"	BIGINT
) ON COMMIT DELETE ROWS;;


