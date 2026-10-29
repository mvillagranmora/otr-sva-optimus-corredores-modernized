SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasivaDetalle";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" AS

SELECT
	 CM."IdCargaMasiva"
	,TCM."IdTipoCargaMasiva"
	,TCM."CodigoTipoCargaMasiva"
	,CM."IdEmpresa"
	,CM."IdUsuario"
  	,CM."FechaCarga"   			-- Fecha Sistema
 	,CM."FechaProceso" 			-- Fecha en que se procesa CM
 	,CM."FechaIngresoRegistro"	-- Fecha de BD al ingreso
 	,CM."NombreArchivo"
	,CM."IdEstado" AS "IdEstadoCargaMasiva"
	,CMD."IdPersona"
	,CMD."IdCargaMasivaDetalle"
    ,CMD."IdTipoMovimiento"
	,CMD."DocumentoIdentidad"
	,PER."NombreCompleto" AS "NombreSocioDeNegocio"
	,CMD."Monto"
	,CMD."ObservacionValidacion"
	,CMD."IdEstado" AS "IdEstadoCargaMasivaDetalle"
	,EST."Nombre" AS "NombreEstadoCargaMasivaDetalle"
	,CMD."IdMovimientoComprobante"  -- Id del movimiento de tesorería generado
	,CMD."IdMoneda"
	,(CASE
		WHEN COALESCE(CMD."CodigoPortafolio",'') = '' THEN POR."CodigoPortafolio"
		ELSE ''
	END) AS "CodigoPortafolio"
	,CMD."IdPortafolio"
	,POR."Proposito" AS "PropositoPortafolio"
	,CMD."Glosa"
	
	-- DATOS DE ORIGEN ENTIDAD EXTERNA
	,CMD."Ori_TipoCuenta" 						--   NVARCHAR(50)
	,CMD."Ori_NumeroCuenta"						--   NVARCHAR(50)
	,CMD."Ori_Moneda"							--   NVARCHAR(50)
	,CMD."Ori_TipoDocumentoIdentidad"			--   NVARCHAR(50)
	,CMD."Ori_DocumentoIdentidad"				--   NVARCHAR(50)

	
	-- DATOS DE ORIGEN Entidad Externa CONCILIACION_RECAUDACION_BCP
	,CMD."BCP_CON_CodigoCuenta"  				-- INTEGER
	,CMD."BCP_CON_CodigoMoneda"  				-- INTEGER
	,CMD."BCP_CON_NumeroCuenta"					-- BIGINT
	,CMD."BCP_CON_IdentificacionDepositante"  	-- NVARCHAR(14)		
	,(CASE
		WHEN COALESCE(CMD."BCP_CON_FechaTransaccion",'') = '' THEN CAST('0001-01-01' AS DATE)   -- NVARCHAR(10)
		ELSE CAST(CMD."BCP_CON_FechaTransaccion" AS DATE)
	END) AS "BCP_CON_FechaTransaccion"
	,CMD."BCP_CON_NumeroOperacion"  			-- BIGINT											
	,CMD."BCP_CON_ReferenciaOperacion"   		-- NVARCHAR(22)											
	,CMD."BCP_CON_IndicadorExtorno"			 	-- NVARCHAR(1)											
	,CMD."BCP_CON_NumeroOperacionBD"			-- BIGINT


FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
INNER JOIN "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = CMD."IdPersona")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CMD."IdEstado")
WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
