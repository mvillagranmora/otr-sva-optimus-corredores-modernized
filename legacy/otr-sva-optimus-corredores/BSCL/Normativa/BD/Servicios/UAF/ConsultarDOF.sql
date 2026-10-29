SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar información para emitir Transacciones con declaración origen de fondos
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/11/2016

"BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    --VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_Monto		 							DECIMAL(27,8)	DEFAULT 0,
	IN p_IdSucursal 							BIGINT			DEFAULT 0,
	IN p_IdEmpleado								BIGINT			DEFAULT 0,
	IN p_IdCliente								BIGINT			DEFAULT 0,
	
    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaDOF	"BSCL.Normativa.BD::NOR.SALIDA_UAFDOF"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
  		DECLARE v_Fecha 							 		 NVARCHAR(10):='';
		DECLARE v_Count,v_IdTrabajoParidad 							 		 BIGINT:=0;
		DECLARE v_IdNormativa 						 		 NVARCHAR(15):='UAF';
		DECLARE v_IdTipoDocumento   		 				 NVARCHAR(10):='UAFDOF';
		DECLARE v_VAL_MONTO_UAFDOF, v_MON_MONTO_UAFDOF 	 	 NVARCHAR(520):='';
		DECLARE v_VAL_MONTO_UF_UAFDOF, v_VAL_MONTO_DOLAR_UAFDOF, v_EVAL_DOF_UAFDOF NVARCHAR(520):='';		
		DECLARE v_ValorMontoDOLAR,	v_ValorMontoUF			 	DECIMAL (26,8):=0;	
	
		DECLARE v_out_error			  						 NVARCHAR(500);
    	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
    	DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
    	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
    	
    	DECLARE v_logo TABLE("LogoCorredora" BLOB);
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora" 
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		---Parametros
		
		
		v_FechaOperacionDesde:=p_FechaOperacionDesde;
  		v_FechaOperacionHasta:=p_FechaOperacionHasta;  		
  		
		IF v_FechaOperacionDesde='' AND v_FechaOperacionHasta='' THEN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			v_FechaOperacionDesde=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			v_FechaOperacionHasta=v_FechaOperacionDesde;
		END IF;

		IF v_FechaOperacionHasta='' THEN
			v_FechaOperacionHasta:=v_FechaOperacionDesde;  		
		END IF;
		
		IF v_FechaOperacionDesde='' THEN
			v_FechaOperacionDesde:=v_FechaOperacionHasta;  		
		END IF;
		
		
	
		----Parametros monto minimo
	
				SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
				
				IF v_Count>0 THEN
					SELECT "Valor" INTO v_VAL_MONTO_UF_UAFDOF
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_VAL_MONTO_UF_UAFDOF:='450';
				END IF;
				v_ValorMontoUF=	CAST(TRIM(v_VAL_MONTO_UF_UAFDOF) AS DECIMAL (26,8));
				
				SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_DOLAR_UAFDOF' AND "IdNormativa"=v_IdNormativa;
				
				IF v_Count>0 THEN
					SELECT "Valor" INTO v_VAL_MONTO_DOLAR_UAFDOF
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_DOLAR_UAFDOF' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_VAL_MONTO_DOLAR_UAFDOF:='15000';
				END IF;
				v_ValorMontoDOLAR=	CAST(TRIM(v_VAL_MONTO_DOLAR_UAFDOF) AS DECIMAL (26,8));

				
		
		
		
		
		
		
		---Periodo 
		SELECT TOP 1 LEFT("Valor",10) INTO v_Fecha
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro"='PLA.FechaSistema' and "IdEmpresa"=p_IdEmpresa;
		
		
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaOperacionDesde='' OR p_FechaOperacionDesde='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaOperacionDesde, 'DD-MM-YYYY');
		END IF;	
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		
		
		v_tablaOperaciones=
		SELECT
			 DOF."IdComprobanteFormaDePago"
			,DOF."FechaLiquidacion"
			,DOF."IdComprobante"
			,DOF."IdTipoOperacion"
			,DOF."IdPersona"
			,DOF."DocumentoIdentidad" 						    AS "Identificacion"
			,DOF."TipoDocumentoIdentidad"		 				AS "TipoDocumento"
			,DOF."Tipo"		 									AS "TipoPersona"
			,DOF."NombreCompleto"
			,DOF."RazonSocial"		
			,DOF."ApellidoPaterno" 
			,DOF."ApellidoMaterno" 
			,DOF."IdTelefonoPrincipal"
			,DOF."IdPersonaTitularPago"
			,DOF."MontoOperacion"
			,DOF."IdMonedaOperacion"
			,DOF."MontoComprobanteFormaDePago"
			,DOF."IdMonedaFormaDePago"
			,DOF."IdMedioDePago"
			,DOF."NombreMedioDePago" 
			,DOF."IdModalidadDePago"
			,DOF."NombreModalidadDePago"
			,DOF."IdEmpleadoDigitador"
			,DOF."CodigoYNombreEmpleado" 							AS "NombreAgente"
			,DOF."IdSucursal"
			,DOF."CodigoSucursal"
			,DOF."NombreSucursal"
			,DOF."IdCliente"
			,DOF."IdDeclaracionOrigenFondos"
			,DOF."Comentario"
			,DOF."Declaracion"
			,DOF."DeclaracionUI"
		FROM "BSCL.Normativa.BD.Vistas::UAF.ConsultarDOF" DOF						
		WHERE 
		TO_DATE(DOF."FechaLiquidacion") BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta)
		AND DOF."IdEmpresa"=:p_IdEmpresa
		AND (
				(DOF."IdSucursalEmpleadoDigitador"=p_IdSucursal OR p_IdSucursal=0)
				AND  
				(DOF."IdEmpleado"=p_IdEmpleado OR p_IdEmpleado=0)
				AND 
				(DOF."IdCliente"=p_IdCliente OR p_IdCliente=0)
			);
	
	
	
			 
		--Verificar si existen operaciones 
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN

			---IGUALAR PARIDADES
			
			
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;			
			
			--UF
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdComprobanteFormaDePago" AS "IdInterno",TO_VARCHAR (CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "Fecha", CAR."IdMonedaOperacion" as "IdMonedaOrigen", 'UF' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."MontoOperacion" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR WHERE CAR."IdMonedaOperacion" NOT IN ('USD','EUR');

			--DOLAR/EURO
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdComprobanteFormaDePago" AS "IdInterno",TO_VARCHAR (CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "Fecha", CAR."IdMonedaOperacion" as "IdMonedaOrigen", 'USD' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."MontoOperacion" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR WHERE CAR."IdMonedaOperacion"  IN ('USD','EUR');

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
				
			
			
			v_out_ParidadDisponibles=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdInterno",outt."IdMonedaDestino", par.*
					FROM :v_tablaOperaciones par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdComprobanteFormaDePago" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			

					
					
					
					
			v_out_ParidadDisponibles=SELECT DISTINCT * FROM  :v_out_ParidadDisponibles;								
			--Igualar montos de Paridades 
			v_MontoOperacionParidad=
				SELECT DISTINCT PAR."MontoDestino" AS "Monto", CAR."IdPersona",CAR."IdComprobanteFormaDePago",PAR."IdMonedaDestino"
				FROM :v_tablaOperaciones CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
				ON (CAR."IdComprobanteFormaDePago"=PAR."IdInterno")
				WHERE PAR."MontoDestino" >= (CASE CAR."IdMonedaOperacion" WHEN  'USD' THEN v_ValorMontoDOLAR
																		  WHEN  'EUR' THEN v_ValorMontoDOLAR
																		  ELSE  v_ValorMontoUF END);
			--Identifica los clientes cuyas operaciones  alcanzan las 450 UF (limite parametro) o 15000 Dolares

						
		--Tabla de Salida
			p_out_TablaDOF=
			SELECT 
				  p_FechaOperacionDesde AS "p_FechaOperacionDesde"
				, p_FechaOperacionHasta AS "p_FechaOperacionHasta"
				/*
				,LOGO."LogoCorredora"
				,GEN."RazonCorredora" 
				,GEN."DireccionCorredora"
				,GEN."RutCorredora" 
				,GEN."TelefonoCorredora" 
				*/
				,OPE."IdComprobanteFormaDePago"
				,OPE."FechaLiquidacion"
				,OPE."IdComprobante"
				,OPE."IdTipoOperacion"
				,OPE."TipoOperacion"
				,OPE."IdPersona"
				,OPE."Identificacion"
				,OPE."TipoDocumento"
				,OPE."TipoPersona"
				,OPE."NombreCompleto"
				,OPE."RazonSocial"
				,OPE."ApellidoPaterno"
				,OPE."ApellidoMaterno"
				,OPE."Telefono"
				,OPE."IdPersonaTitularPago"
				,OPE."NombreCompletoPagador"
				,OPE."IdentificacionPagador"
				,OPE."TipoDocumentoPagador"				
				,OPE."MontoOperacion"
				,OPE."IdMonedaOperacion"
				,OPE."MontoComprobanteFormaDePago"
				,OPE."IdMonedaFormaDePago"
				,OPE."MonedaFormaDePago"
				,OPE."IdMedioDePago"
				,OPE."NombreMedioDePago" 
				,OPE."IdModalidadDePago"
				,OPE."NombreModalidadDePago"
				,OPE."IdEmpleadoDigitador"
				,OPE."NombreAgente"
				,OPE."IdSucursal"
				,OPE."CodigoSucursal"
				,OPE."NombreSucursal"
				,OPE."IdCliente"
				,OPE."IdDeclaracionOrigenFondos"
				,OPE."Comentario"
				,OPE."Declaracion"
				,OPE."DeclaracionUI"
			FROM	
			(			
	     			SELECT 
						 OP."IdComprobanteFormaDePago"
						,OP."FechaLiquidacion"
						,OP."IdComprobante"
						,OP."IdTipoOperacion"
						,OPER."Nombre" AS "TipoOperacion"
						,OP."IdPersona"
						,OP."Identificacion"
						,OP."TipoDocumento"
						,OP."TipoPersona"
						,OP."NombreCompleto"
						,OP."RazonSocial"
						,OP."ApellidoPaterno"
						,OP."ApellidoMaterno"
						,COALESCE(TEL."Numero",'') 						AS "Telefono"
						,OP."IdPersonaTitularPago"
						,COALESCE(PERPAG."NombreCompleto",'') 			AS "NombreCompletoPagador"
						,COALESCE(PERPAG."DocumentoIdentidad",'')		AS "IdentificacionPagador"
						,COALESCE(PERPAG."TipoDocumentoIdentidad",'') 	AS "TipoDocumentoPagador"
						,OP."MontoOperacion"
						,OP."IdMonedaOperacion"
						,OP."MontoComprobanteFormaDePago"
						,OP."IdMonedaFormaDePago"
						,Moneda."Nombre" 					AS "MonedaFormaDePago"
						,OP."IdMedioDePago"
						,OP."NombreMedioDePago" 
						,OP."IdModalidadDePago"
						,OP."NombreModalidadDePago"
						,OP."IdEmpleadoDigitador"
						,OP."NombreAgente"
						,OP."IdSucursal"
						,OP."CodigoSucursal"
						,OP."NombreSucursal"
						,OP."IdCliente"
						,OP."IdDeclaracionOrigenFondos"
						,OP."Comentario"
						,OP."Declaracion"
						,OP."DeclaracionUI"
	     			FROM :v_tablaOperaciones OP 
	     			INNER JOIN :v_MontoOperacionParidad PA
	     				ON (OP."IdPersona" =PA."IdPersona"AND OP."IdComprobanteFormaDePago"=PA."IdComprobanteFormaDePago")
     				LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER 
     					ON OPER."IdTipoOperacion" = OP."IdTipoOperacion"
    				LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda
						ON (Moneda."IdMoneda" = OP."IdMonedaFormaDePago")
					LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERPAG
						ON (OP."IdPersonaTitularPago"=PERPAG."IdPersona")		
					LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono"	TEL				
						ON (OP."IdTelefonoPrincipal"=TEL."IdTelefono")
					
			) OPE;	
		ELSE

		
		p_out_TablaDOF=SELECT
				 '' AS "p_FechaOperacionDesde"
				,'' AS "p_FechaOperacionHasta"
				/*
				,'' AS "LogoCorredora"
				,'' AS "RazonCorredora" 
				,'' AS "DireccionCorredora" 
				,'' AS "RutCorredora" 
				,'' AS "TelefonoCorredora" 
				*/
				,  0 AS "IdComprobanteFormaDePago"
				,'' AS "FechaLiquidacion"
				, 0 AS "IdComprobante"
				,'' AS "IdTipoOperacion"
				,'' AS "TipoOperacion"
				, 0 AS "IdPersona"
				,'' AS "Identificacion"
				,'' AS "TipoDocumento"
				,'' AS "TipoPersona"
				,'' AS "NombreCompleto"
				,'' AS "RazonSocial"
				,'' AS "ApellidoPaterno"
				,'' AS "ApellidoMaterno"
				,'' AS "Telefono"
				, 0 AS "IdPersonaTitularPago"
				,'' AS "NombreCompletoPagador"
				,'' AS "IdentificacionPagador"
				,'' AS "TipoDocumentoPagador"
				, 0 AS "MontoOperacion"
				,'' AS "IdMonedaOperacion"
				, 0 AS "MontoComprobanteFormaDePago"
				,'' AS "IdMonedaFormaDePago"
				,'' AS "MonedaFormaDePago"				
				,'' AS "IdMedioDePago"
				,'' AS "NombreMedioDePago" 
				,'' AS "IdModalidadDePago"
				,'' AS "NombreModalidadDePago"
				, 0 AS "IdEmpleadoDigitador"
				,'' AS "NombreAgente"
				, 0 AS "IdSucursal"
				,'' AS "CodigoSucursal"
				,'' AS "NombreSucursal"
				, 0 AS "IdCliente"
				, 0 AS "IdDeclaracionOrigenFondos"
				,'' AS "Comentario"
				,'' AS "Declaracion"
				,'' AS "DeclaracionUI"
				FROM DUMMY WHERE 1=2;	
		
		END IF;
		--v_Resultado =
		SELECT * FROM :p_out_TablaDOF
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo ;
		
		--SELECT * FROM :v_Resultado;
END; 
