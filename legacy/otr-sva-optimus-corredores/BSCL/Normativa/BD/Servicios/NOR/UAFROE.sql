SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::UAFROE";
 
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 29/11/2016

"BSCL.Normativa.BD.Servicios.NOR::UAFROE" ( 
    --VARIABLES DEL PROCESO
	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_UAFROE" --Se debe llamar siemte p_out_TablaSalida
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
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
				  CASE WHEN SOL."TipoParametro"='MONTO'		THEN "Valor" ELSE NULL END AS "MONTO"
				, CASE WHEN SOL."TipoParametro"='FINI'		THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='FTER'		THEN "Valor" ELSE NULL END AS "FTER"
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
				FROM :p_TablaParametros SOL;
		END IF;
		
	
	
		v_DatosAgrupado =SELECT
							  MAX(SOL."MONTO") 		AS "MONTO"
							, MAX(SOL."FINI") 		AS "FINI"
							, MAX(SOL."FTER")		AS "FTER"
							FROM :v_Datos  SOL;
						

		SELECT TOP 1 "FINI","FTER","MONTO"  INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_Monto
		FROM :v_DatosAgrupado;	
		
		v_FechaOperacionDesde:=COALESCE(v_FechaOperacionDesde,'');
		v_FechaOperacionHasta:=COALESCE(v_FechaOperacionHasta,'');
		v_monto:=COALESCE(v_monto,0);		
						
		CALL "BSCL.Normativa.BD.Servicios.UAF::ConsultarROE"
		(	p_IdEmpresa 						=>v_IdEmpresa,
			p_FechaOperacionDesde 				=>v_FechaOperacionDesde,
			p_FechaOperacionHasta 				=>v_FechaOperacionHasta,
			p_Monto 							=>v_Monto,
			p_out_TablaROE 						=>v_out_Tabla
		); 
		
		p_out_TablaSalida = SELECT 
		--- DATOS SUCURSAL
		"RegionSucursal"
		,"LocalidadSucursal"
		,"CalleSucursal"
		,"NumeroSucursal"
		,"DeptoSucursal"
		,"TelefonoSucursal"
		--- DATOS OPERACION
		,"IdComprobante"
		,TO_DATE ("FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,"IdPersona"
		,"IdPersonaTitularPago"
		---ENTRANTE
	
		,"IdMonedaEntrante"
		,"MontoEntrante"
		,"DenBilleteEntrante"
		---SALIENTE
		,"IdMonedaSaliente"
		,"MontoSaliente"
		,"DenBilleteSaliente"
		,"NumComprobante"
		,"Fecha"
		----CLIENTE		
		,"Identificacion"
		,"TipoDocumento"
		,"RazonSocial"
		,"ApellidoPaterno"
		,"ApellidoMaterno"
		,"Nacionalidad"
		,"Habitualidad"
		,"Pais"
		,"Region"
		,"Comuna"
		,"Calle"
		,"Numero"
		,"Departamento"
		,"Telefono"
		---REPRESENTANTE PAGADOR
		,"RepIdentificacion"
		,"RepTipoDocumento"
		,"RepRazonSocial"
		,"RepApellidoPaterno"
		,"RepApellidoMaterno"
		,"RepNacionalidad"
		,"RepHabitualidad"
		,"RepPais"
		,"RepRegion"
		,"RepComuna"
		,"RepCalle"
		,"RepNumero"
		,"RepDepartamento"
		,"RepTelefono"
		--,"IdTipoMovimiento"
		--,"IdMoneda"
		--,"Monto"
		 FROM :v_out_Tabla;
		 
		SELECT * FROM  :p_out_TablaSalida;		 
		
END; 
