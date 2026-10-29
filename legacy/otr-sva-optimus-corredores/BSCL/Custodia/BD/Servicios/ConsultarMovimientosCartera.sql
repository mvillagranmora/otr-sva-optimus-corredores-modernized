SET SCHEMA "BSCL";  
DROP procedure "BSCL.Custodia.BD.Servicios::ConsultarMovimientosCartera";
  
CREATE procedure "BSCL.Custodia.BD.Servicios::ConsultarMovimientosCartera" 
(	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa					BIGINT			--ID Empresa destino
	,IN  p_IdUsuario					BIGINT			--ID del usuario que solicita el servicio
	,IN  p_IdModulo						NVARCHAR(3)  		--ID del Modulo que lo invoca	

	-- Parametros entrada del servicio publico
	,IN p_IdCliente							BIGINT 			default 0
	,IN p_IdPortafolio						BIGINT			default 0
	,IN p_FolioCartera						BIGINT 			default 0
	,IN p_IdClasificacionTipoInstrumento 	NVARCHAR(3) 	default ''
	,IN p_IdTipoInstrumento					NVARCHAR(3) 	default ''
	,IN p_IdInstrumento						BIGINT 			default 0
	,IN p_IdBoveda							NVARCHAR(3) 	default ''
	,IN p_IdMoneda							NVARCHAR(3) 	default ''
	,IN p_IdConceptoING						NVARCHAR(6) 	default ''
	,IN p_IdConceptoEGR						NVARCHAR(6) 	default ''
	,IN p_FechaDesde 				    	DATE			default '0001-01-01' 		
	,IN p_FechaHasta 				    	DATE			default '0001-01-01'
	
	,OUT p_out_Salida 							"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraMovimientos" default empty
)
AS
begin

	v_carteras = 		

	SELECT distinct "FolioCartera", MIN("IdCartera") as "IdCartera", MIN("FechaOperacion") as "INGFecha"
	FROM "BSCL.Custodia.BD::CUS.Cartera" 
	where 
		"IdEmpresa" = p_IdEmpresa
		and "IdEstado" <> 'ANU'
		and "FolioCartera" = case when p_FolioCartera = 0 then "FolioCartera" else p_FolioCartera end
		and "IdCliente" = case when p_IdCliente = 0 then "IdCliente" else p_IdCliente end
		and "IdPortafolio" = case when p_IdPortafolio = 0 then "IdPortafolio" else p_IdPortafolio end
		and "IdInstrumento" = case when p_IdInstrumento = 0 then "IdInstrumento" else p_IdInstrumento end
		and "IdBoveda" = case when p_IdBoveda = '' then "IdBoveda" else p_IdBoveda end
		and "IdMonedaInstrumento" = case when p_IdMoneda = '' then "IdMonedaInstrumento" else p_IdMoneda end
		and "FechaOperacion" >= case when p_FechaDesde = '0001-01-01' then "FechaOperacion" else p_FechaDesde end
		and "FechaOperacion" <= case when p_FechaHasta = '0001-01-01' then "FechaOperacion" else p_FechaHasta end
		and "IdConcepto" = case when p_IdConceptoING = '' then "IdConcepto" else p_IdConceptoING end
		
	GROUP BY "FolioCartera";
			
			
	p_out_Salida =
		Select b.* 
		from :v_carteras as a
		inner join "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraMovimientos" as b on a."FolioCartera" = b."FolioCartera"
		where 
			("EGRIdConcepto" <> '' and "EGRIdConcepto" = case when p_IdConceptoEGR = '' then "EGRIdConcepto" else p_IdConceptoEGR end)
			or 
			("EGRIdConcepto" = '')
		order by "FolioCartera" desc, "IdCartera";

end;