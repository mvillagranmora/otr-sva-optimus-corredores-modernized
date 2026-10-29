SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::UAFDOF";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 14/11/2016

"BSCL.Normativa.BD.Servicios.NOR::UAFDOF" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_UAFDOF" 
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdSucursal,v_IdEmpleado INT:=0;
		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		DECLARE	v_Monto		 DECIMAL(27,8):=0;
		
		IF p_IdSolicitudCircular>0 THEN
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='MONTO'				THEN "Valor" ELSE NULL END AS "MONTO"
				, CASE WHEN SOL."TipoParametro"='FINI'				THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='FTER'				THEN "Valor" ELSE NULL END AS "FTER"
				, CASE WHEN SOL."TipoParametro"='IDSUCURSAL'		THEN "Valor" ELSE 0 END AS "IDSUCURSAL"
				, CASE WHEN SOL."TipoParametro"='IDEMPLEADO'		THEN "Valor" ELSE 0 END AS "IDEMPLEADO"
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		END IF;	
	
		--Valores tabla parametros desde UI VISOR
		SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
		if v_Count>0 THEN
			SELECT TOP 1  "Valor" INTO v_IdEmpresa
				FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
				
			v_Datos = SELECT
				  CASE WHEN SOL."TipoParametro"='MONTO'			THEN "Valor" ELSE NULL END AS "MONTO"
				, CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
				, CASE WHEN SOL."TipoParametro"='IdSucursal'	THEN "Valor" ELSE 0 END AS "IDSUCURSAL"
				, CASE WHEN SOL."TipoParametro"='IdEmpleado'	THEN "Valor" ELSE 0 END AS "IDEMPLEADO"
				FROM :p_TablaParametros SOL;
		END IF;
		
	
	
		v_DatosAgrupado =SELECT
							  MAX(SOL."MONTO") 			AS "MONTO"
							, MAX(SOL."FINI") 			AS "FINI"
							, MAX(SOL."FTER")			AS "FTER"
							, MAX(SOL."IDSUCURSAL")		AS "IDSUCURSAL"
							, MAX(SOL."IDEMPLEADO")		AS "IDEMPLEADO"
							FROM :v_Datos  SOL;
						

		SELECT TOP 1 "FINI","FTER","MONTO","IDSUCURSAL","IDEMPLEADO" INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_Monto,v_IdSucursal,v_IdEmpleado
		FROM :v_DatosAgrupado;	
		
		v_FechaOperacionDesde:=COALESCE(v_FechaOperacionDesde,'');
		v_FechaOperacionHasta:=COALESCE(v_FechaOperacionHasta,'');
		v_monto:=COALESCE(v_monto,0);		
						
		CALL "BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF"
		(	p_IdEmpresa 						=>v_IdEmpresa,
			p_FechaOperacionDesde 				=>v_FechaOperacionDesde,
			p_FechaOperacionHasta 				=>v_FechaOperacionHasta,
			p_Monto 							=>v_Monto,
			p_IdSucursal 						=>v_IdSucursal,
			p_IdEmpleado						=>v_IdEmpleado,
			p_out_TablaDOF 						=>v_out_Tabla
		); 
		
		p_out_TablaSalida = SELECT 
		"p_FechaOperacionDesde"
		,"p_FechaOperacionHasta"
		/*
		,"LogoCorredora"
		,"RazonCorredora" 
		,"DireccionCorredora" 
		,"RutCorredora" 
		,"TelefonoCorredora" 
		*/
		,"IdComprobanteFormaDePago"
		,TO_DATE ("FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,"IdComprobante"
		,"IdTipoOperacion"
		,"TipoOperacion"
		,"IdPersona"
		,"Identificacion"
		,"TipoDocumento"
		,"TipoPersona"
		,"NombreCompleto"
		,"RazonSocial"
		,"ApellidoPaterno"
		,"ApellidoMaterno"
		,"Telefono"
		,"IdPersonaTitularPago"
		,"NombreCompletoPagador"
		,"IdentificacionPagador"
		,"TipoDocumentoPagador"
		,"MontoOperacion"
		,"IdMonedaOperacion"
		,"MontoComprobanteFormaDePago"
		,"IdMonedaFormaDePago"
		,"MonedaFormaDePago"
		,"IdMedioDePago"
		,"NombreMedioDePago" 
		,"IdModalidadDePago"
		,"NombreModalidadDePago"
		,"IdEmpleadoDigitador"
		,"NombreAgente"
		,"IdSucursal"
		,"CodigoSucursal"
		,"NombreSucursal"
		,"IdCliente"
		,"IdDeclaracionOrigenFondos"
		,"Comentario"
		,"Declaracion"
		,"DeclaracionUI"
		 FROM :v_out_Tabla WHERE "Declaracion"='S';
		 
		SELECT * FROM  :p_out_TablaSalida;		 
		
END; 
