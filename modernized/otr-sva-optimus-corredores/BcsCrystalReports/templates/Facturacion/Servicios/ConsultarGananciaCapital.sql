SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Facturacion.CrystalReports.Servicios::ConsultarGananciaCapital";
CREATE PROCEDURE "BSCL.Facturacion.CrystalReports.Servicios::ConsultarGananciaCapital" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_FechaConsulta							NVARCHAR(10),			 --'YYYY-MM-DD'
	IN p_Datos           							TABLE ( "IdCliente" BIGINT,"Folio" BIGINT  ),
	
	OUT p_Salida   TABLE (

			"FechaComprobante"				NVARCHAR(10),
			"Folio"							BIGINT,	
			"Instrumento"					NVARCHAR(100),
			"Cantidad"						DECIMAL(26,8),
			"Precio"						DECIMAL(26,8),
			"Monto"							DECIMAL(26,8),
			"IdMoneda"						NVARCHAR(10),
			"MontoRetencionGananciaCap"		DECIMAL(26,8),
			"MontoRetencionGananciaCapCLP"	DECIMAL(26,8),
			"FechaIngresoRegistro"          TIMESTAMP,
			"NroLinea"						BIGINT,	
			"IdCliente"						BIGINT,
			"NombreCompletoCliente"			NVARCHAR(400),	
			"DocumentoIdentidadCliente"		NVARCHAR(40),	
			"DireccionCliente"				NVARCHAR(400),	
			"ComunaCliente"					NVARCHAR(60),	
			"CiudadCliente"					NVARCHAR(60),	
			"FechainformeDesde"				NVARCHAR(10),
			"FechainformeHasta"				NVARCHAR(10),
			"PorcentajeRetencion"			NVARCHAR(520),
			-----------------------------------------	
			"RazonSocialCorredora"          NVARCHAR(100),
			"DireccionCorredora"            NVARCHAR(300),
			"RutCorredora"   				NVARCHAR(40),
			"GiroCorredor"					NVARCHAR(500),
			"rut_representante"				NVARCHAR(40),
			"nombre_representante"			NVARCHAR(100),
			-------------------------------------------
			
			"LogoCorredora"                 BLOB,
			"FirmaCorredora"                BLOB
	
	)
	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_Logo  TABLE("LogoCorredora" BLOB);
		DECLARE v_Firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_countTab,v_countOpt ,v_IdTrabajoParidad BIGINT:=0;
	
		DECLARE v_FechaSistema  NVARCHAR(10);
	
		--p_Datos = SELECT 249194 AS "IdCliente",0 AS "Folio" FROM dummy;

		v_Logo =
			SELECT TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_Firma =
			SELECT TOP 1 "Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;

	
		/*Datos del Corredor*/
		v_DatosCorredor = select 
		"RazonSocial" AS "RazonSocialCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora",
		"IdEmpresa"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;		
		
		v_DatosCorredor2=
			SELECT 
			  GIR."Nombre" AS "GiroCorredor", 
			  EMP."IdEmpresa"
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona" 
			LEFT  JOIN  "BSCL.Plataforma.BD::REG.Giro" AS GIR  ON (PER."IdGiroPrincipal"=GIR."IdGiro")
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;
		
		
		v_datos_representante=
        SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		SELECT COUNT(1) INTO v_countTab FROM :p_Datos WHERE "IdCliente" IS NOT NULL;
		
		if v_countTab>0  THEN
			v_cliente=SELECT  DISTINCT "IdCliente"FROM :p_Datos WHERE "IdCliente" IS NOT NULL;
		ELSE
			v_cliente=SELECT "IdCliente" FROM "BSCL.Facturacion.BD::FAC.Comprobante"   WHERE  TO_VARCHAR ( "FechaComprobante", 'YYYY-MM')=TO_VARCHAR (:p_FechaConsulta, 'YYYY-MM') and "CargoGananciaCap"='C' ;
			
		END IF;
		
		
		/*Datos del Cliente*/
		v_datosClientes = SELECT  CLI."IdPersona",CLI."IdCliente",CLI."NombreCompleto" AS "NombreCompletoCliente", CLI."DocumentoIdentidad" AS "DocumentoIdentidadCliente",CLI."IdEmpleadoEjecutivo", CLI."IdTelefonoPrincipal", CLI."IdTipoPersona",TEL."TelefonoDestinatario" 
								 ,COALESCE(EJE."EjecutivoNombre",'') AS "EjecutivoNombre",COALESCE(EJE."EjecutivoDireccionElectronica",'') AS "EjecutivoDireccionElectronica"
								 ,COALESCE(DIR."Direccion",'') AS  "Direccion",COALESCE(DIR."Comuna",'') AS  "Comuna",COALESCE(DIR."Ciudad",'') AS  "Ciudad"
								 ,PER."Sexo", PER."EstadoCivil"
							 FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
							 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = CLI."IdPersona")
							 INNER JOIN (SELECT DISTINCT "IdCliente" FROM :v_cliente) FIL ON (FIL."IdCliente"=CLI."IdCliente")
							 LEFT JOIN (
							 	SELECT EMP."NombreCompleto" AS "EjecutivoNombre",COALESCE(DIR."Direccion",'') AS "EjecutivoDireccionElectronica",EMP."IdEmpleado" 
								FROM   "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP 
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR
								ON (EMP."IdDireccionElectronicaPrincipal"=DIR."IdDireccionElectronica")
							 ) AS EJE ON (EJE."IdEmpleado"=CLI."IdEmpleadoEjecutivo" )
						  
							 LEFT JOIN (
								SELECT 
								RANK() over (partition by  "IdPersona" order by ("Predeterminado" || CAST ("IdTelefono" AS NVARCHAR(14)))  desc) as RANK
								,"Numero" AS "TelefonoDestinatario","IdPersona"  FROM "BSCL.SocioNegocio.BD::PER.Telefono"
								) AS TEL ON (TEL.RANK=1 AND TEL."IdPersona"=CLI."IdPersona" )
								
							 LEFT JOIN (
								SELECT 
								RANK() over (partition by  "IdPersona" order by ("Predeterminado" || CAST ("IdDireccionFisica" AS NVARCHAR(14)))  desc) as RANK
								,CONCAT(CONCAT(COALESCE("Calle",''),' '),COALESCE(CAST("Numero" AS VARCHAR),'')) AS "Direccion",COALESCE("NombreDivisionPais",'') AS "Comuna"
								,COALESCE("NombreCiudad",'') AS "Ciudad" ,"IdPersona"  
								FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
								) AS DIR ON (DIR.RANK=1 AND DIR."IdPersona"=CLI."IdPersona" );	
		--Zona comun	

		
		v_DatosDetalle =
			SELECT TO_VARCHAR ( CM."FechaComprobante", 'DD-MM-YYYY') AS "FechaComprobante"
				,CD."IdComprobante" AS "Folio"
				,CD."NemotecnicoInstrumento" AS "Instrumento"
				,CD."Cantidad"
				,CD."Precio"
				,CD."Monto"
				,CD."IdMoneda"
				,CD."MontoRetencionGananciaCap"
				,CM."FechaIngresoRegistro"
				,CD."NroLinea"
				,CM."IdCliente"
				,cli."NombreCompletoCliente"
				,cli."DocumentoIdentidadCliente"
				,cli."Direccion" AS "DireccionCliente"
				,cli."Comuna" AS "ComunaCliente"
				,cli."Ciudad" AS "CiudadCliente"
				,CD."IdComprobanteDetalle"
				--,CM.*
			FROM "BSCL.Facturacion.BD::FAC.Comprobante" CM
			INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CD ON (CD."IdComprobante"=CM."IdComprobante")
			INNER JOIN :v_datosClientes cli ON (cli."IdCliente"=CM."IdCliente")
			WHERE CM."CargoGananciaCap"='C'
			AND TO_VARCHAR ( CM."FechaComprobante", 'YYYY-MM')=TO_VARCHAR (:p_FechaConsulta, 'YYYY-MM')--'2022-08'
			AND (EXISTS (SELECT 1 FROM  :p_Datos DA WHERE (DA."IdCliente"=CM."IdCliente" AND (DA."Folio"=CM."IdComprobante" OR DA."Folio"=0 ) ) )
				OR v_countTab=0
			    )
			; 
			
		
			--Verificar si existen operaciones 
		SELECT COUNT(1) INTO v_countOpt FROM :v_DatosDetalle;
 				 
		IF v_countOpt > 0 THEN	
		
			---IGUALAR PARIDADES			
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdComprobanteDetalle" AS "IdInterno",TO_VARCHAR (CAR."FechaIngresoRegistro", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."MontoRetencionGananciaCap" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_DatosDetalle CAR;
			
			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
				
				v_DatosDetalle_Paridad=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoRetencionGananciaCapCLP",outt."IdInterno", par.*
					FROM :v_DatosDetalle par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdComprobanteDetalle" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			
			
				p_Salida = SELECT
						 "Detalle"."FechaComprobante"
						,"Detalle"."Folio"
						,"Detalle"."Instrumento"
						,"Detalle"."Cantidad"
						,"Detalle"."Precio"
						,"Detalle"."Monto"
						,"Detalle"."IdMoneda"
						,"Detalle"."MontoRetencionGananciaCap"
						,"Detalle"."MontoRetencionGananciaCapCLP"
						,"Detalle"."FechaIngresoRegistro"
						,"Detalle"."NroLinea"
						,"Detalle"."IdCliente"
						,"Detalle"."NombreCompletoCliente"
						,"Detalle"."DocumentoIdentidadCliente"
						,"Detalle"."DireccionCliente"
						,"Detalle"."ComunaCliente"
						,"Detalle"."CiudadCliente"
						,'01-' || TO_VARCHAR (:p_FechaConsulta, 'MM-YYYY')	AS	"FechainformeDesde"
						,TO_VARCHAR (LAST_DAY (TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD')), 'DD-MM-YYYY')	AS  "FechainformeHasta"
						,(SELECT top 1 par."Valor" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" par  where par."CodigoParametro"='IGK_POR_RETEN_NORESIDENTE') AS "PorcentajeRetencion"
						-------------------------
						,"Corredor"."RazonSocialCorredora"
						,"Corredor"."DireccionCorredora"
						,"Corredor"."RutCorredora"
						,"Corredor2"."GiroCorredor"
						,"Rep"."rut_representante"
						,"Rep"."nombre_representante"
						
						,CASE when v_countTab = 1 then "Logo"."LogoCorredora" else '' end as "LogoCorredora"
						,CASE when v_countTab = 1 then "Firma"."FirmaCorredora" else '' end as "FirmaCorredora"
		 
							 FROM :v_DatosDetalle_Paridad AS "Detalle"
							 LEFT JOIN :v_DatosCorredor AS "Corredor" 	ON "Corredor"."IdEmpresa"=:p_IdEmpresa
							 LEFT JOIN :v_DatosCorredor2 AS "Corredor2" 	ON "Corredor2"."IdEmpresa"=:p_IdEmpresa
					         LEFT JOIN :v_Logo 			AS "Logo" 				ON "Logo"."LogoCorredora" IS NOT NULL
							 LEFT JOIN :v_Firma 		AS "Firma" 				ON "Firma"."FirmaCorredora" IS NOT NULL
							 LEFT JOIN :v_datos_representante AS "Rep" 	ON "Rep"."rut_representante" IS NOT NULL
						ORDER BY "Detalle"."FechaIngresoRegistro","Detalle"."Folio","Detalle"."NroLinea";
						
			ELSE
			
						
				p_Salida = SELECT
						 ''AS "FechaComprobante"
						,0 AS "Folio"
						,''AS "Instrumento"
						,0 AS "Cantidad"
						,0 AS "Precio"
						,0 AS "Monto"
						,''AS "IdMoneda"
						,0 AS "MontoRetencionGananciaCap"
						,0 AS "MontoRetencionGananciaCapCLP"
						,''AS "FechaIngresoRegistro"
						,0 AS "NroLinea"
						,0 AS "IdCliente"
						,"Detalle"."NombreCompletoCliente"
						,"Detalle"."DocumentoIdentidadCliente"
						,"Detalle"."Direccion" AS "DireccionCliente"
						,"Detalle"."Comuna" AS "ComunaCliente"
						,"Detalle"."Ciudad" AS "CiudadCliente"
						,'01-' || TO_VARCHAR (:p_FechaConsulta, 'MM-YYYY')	AS	"FechainformeDesde"
						,TO_VARCHAR (LAST_DAY (TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD')), 'DD-MM-YYYY')	AS  "FechainformeHasta"
						,(SELECT top 1 par."Valor" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" par  where par."CodigoParametro"='IGK_POR_RETEN_NORESIDENTE') AS "PorcentajeRetencion"
		
						-------------------------
						,"Corredor"."RazonSocialCorredora"
						,"Corredor"."DireccionCorredora"
						,"Corredor"."RutCorredora"
						,"Corredor2"."GiroCorredor"
						,"Rep"."rut_representante"
						,"Rep"."nombre_representante"
						
						,"Logo"."LogoCorredora"   as "LogoCorredora"
						,"Firma"."FirmaCorredora" as "FirmaCorredora"
		 
						 FROM :v_datosClientes AS "Detalle"
							 LEFT JOIN :v_DatosCorredor AS "Corredor" 	ON "Corredor"."IdEmpresa"=:p_IdEmpresa
							 LEFT JOIN :v_DatosCorredor2 AS "Corredor2" 	ON "Corredor2"."IdEmpresa"=:p_IdEmpresa
					         LEFT JOIN :v_Logo 			AS "Logo" 				ON "Logo"."LogoCorredora" IS NOT NULL
							 LEFT JOIN :v_Firma 		AS "Firma" 				ON "Firma"."FirmaCorredora" IS NOT NULL
							 LEFT JOIN :v_datos_representante AS "Rep" 	ON "Rep"."rut_representante" IS NOT NULL;
						
						
			END IF;	
				SELECT * FROM  :p_Salida;
				
				
END;
