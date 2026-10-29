SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodiaConsolidada";
GO 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información para el Certificado de custodia y Carta Circularizacion.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 11/10/2016

"BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodiaConsolidada" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa				BIGINT,
    IN p_IdCliente				BIGINT,
    IN p_IdPortafolio			BIGINT,
    IN p_FechaConsulta			NVARCHAR(10),			 --'YYYY-MM-DD'

    --Variables seleccion UI (Opcionales) --> Se determinan segun configuracion segun p_FormaSalida
/*
    IN p_CusRV 						NVARCHAR(1) DEFAULT '', --- 	Mercados en custodia
	IN p_CusRF 						NVARCHAR(1) DEFAULT '',
	IN p_CusIIF 					NVARCHAR(1) DEFAULT '',
	IN p_CusFM						NVARCHAR(1) DEFAULT '',
    IN p_CusConsideraMontoComprom 	NVARCHAR(1) DEFAULT '', --- 	UI_CONSIDERA_MONTO_COMPROMETIDO
    IN p_incluirOperPactos 			NVARCHAR(1) DEFAULT '', --- 	UI_INCLUIR_OPERACION_PACTOS
    IN p_incluirOperSimul  			NVARCHAR(1) DEFAULT '', --- 	UI_INCLUIR_OPERACION_SIMULTANEA
   */
    IN p_FormaSalida				NVARCHAR(14) DEFAULT 'CERTIFICADO', --'CARTA'
    IN p_UnificarPortafolios		NVARCHAR(1) DEFAULT 'N',
    IN p_IdUsuario					BIGINT DEFAULT NULL

    --VARIABLES DEL PROCESO

    --VARIABLES DE SALIDA OBLIGATORIAS

	--OUT p_out_TablaPEP	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_Count,v_idPersona,v_IdEmpleadoEjecutivo	 BIGINT:=0;
		DECLARE v_NombreDestinatario,v_EjecutivoNombre, v_EjecutivoDireccionElectronica		NVARCHAR(100):='';
		DECLARE v_Direccion									NVARCHAR(80):='';
		DECLARE v_Comuna									NVARCHAR(60):='';
		DECLARE v_Ciudad									NVARCHAR(60):='';		
		DECLARE v_Rut										NVARCHAR(12):='';	
		DECLARE v_CodigoPortafolio							NVARCHAR(100):='';	
		DECLARE v_IdNormativa								NVARCHAR(15):='CIRCULARIZACION';
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_PeriodoCarta								NVARCHAR(6):='';
		DECLARE v_SIM,v_PAC,v_VTACOR						NVARCHAR(3):='';

		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_AgregarCuentaCaja		NVARCHAR(1):='N';
		DECLARE v_CuentaInversionAConsultar		TABLE ("IdEjecutivo" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3));
		DECLARE v_SaldoInicialFinal			 	TABLE("IdCuentaInversion"   BIGINT,
    										  	 "SaldoEfectivoInicial"		DECIMAL(26,8),
    										  	 "SaldoEfectivoFinal"		DECIMAL(26,8),
    										  	 "SaldoMovimientoInicial"	DECIMAL(26,8),
    										  	 "SaldoMovimientoFinal"		DECIMAL(26,8));
	  	DECLARE v_SaldoPorFechaEfectiva      	TABLE("IdEjecutivo"			BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
    											 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,  
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazo"				DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100));
		DECLARE v_SaldoPorFechaMovimiento		TABLE ("IdEjecutivo"		BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
												 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "PropositoPortafolio"		NVARCHAR(100),
                                              	 "CodigoPortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazo"				DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100));
        DECLARE v_InformacionContador  		INT;
	 	DECLARE v_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
	 	DECLARE v_AdvertenciasContador      INT;
		DECLARE v_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));	
		DECLARE v_ErroresContador           INT;
		DECLARE v_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000));
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
				
		/*Datos del Cliente*/
		SELECT TOP 1 "IdPersona","NombreCompleto", "DocumentoIdentidad","IdEmpleadoEjecutivo" INTO v_idPersona,v_NombreDestinatario,v_Rut,v_IdEmpleadoEjecutivo FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
		WHERE "IdCliente"=p_IdCliente;
		--Direccion
		SELECT TOP 1  CONCAT(CONCAT(COALESCE("Calle",''),' '),COALESCE(CAST("Numero" AS VARCHAR),''))
		,"NombreDivisionPais","NombreCiudad"
		INTO v_Direccion,v_Comuna,v_Ciudad FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
		WHERE "IdPersona"=v_idPersona;
		---Portafolio
		IF p_UnificarPortafolios = 'N' THEN
			SELECT TOP 1  "CodigoPortafolio" INTO v_CodigoPortafolio FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
			WHERE "IdPortafolio"=p_IdPortafolio;
		ELSE
			--UNIFICAR
		END IF;
		
		--Ejecutivo

		SELECT EMP."NombreCompleto" ,COALESCE(DIR."Direccion",'') INTO v_EjecutivoNombre, v_EjecutivoDireccionElectronica 
		FROM   "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP 
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR
		ON (EMP."IdDireccionElectronicaPrincipal"=DIR."IdDireccionElectronica")
		where EMP."IdEmpleado"=v_IdEmpleadoEjecutivo;



		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora",
		 v_EjecutivoNombre AS "EjecutivoNombre",
		 v_EjecutivoDireccionElectronica AS "EjecutivoDireccionElectronica"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
			--------------------
			--PARAMETROS : Segun tipo de salida
			--------------------		
		v_tablaInstrumentos=SELECT NULL AS "IdClasificacionTipoInstrumento" FROM DUMMY;
		IF p_FormaSalida='CARTA' THEN
----FormaSalida = 'CARTA' - Circularizacion

		v_tablaInstrumentos=SELECT 	(CASE "Valor"
										WHEN 'registroRV' 		THEN 'RV'
										WHEN 'registroRF' 		THEN 'RF'
										WHEN 'registroIIF' 		THEN 'IF'
										WHEN 'registroFFMM' 	THEN 'FM'
										ELSE NULL
									END) AS 	"IdClasificacionTipoInstrumento"
									FROM "BSCL.Normativa.BD::CIR.Parametro" WHERE "Periodo"=''
									AND "Codigo"='MERCADO' AND "IdEstado"='ACT';
			
		ELSE
----FormaSalida = 'CERTIFICADO'


		END IF;
		
		
		/*FLUJO  CUSTODIA*/
		v_tablaClientes=SELECT "IdCliente", "IdPortafolio" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" WHERE "IdCliente" = p_IdCliente;
								
		CALL "BSCL.Normativa.BD.Servicios::CIR.ConsultarCustodia"(
				p_IdEmpresa                   => p_IdEmpresa,
				p_FechaConsulta               => TO_VARCHAR (:p_FechaConsulta, 'YYYY-MM-DD'),
				p_FormaCustodia				  => 'CUSTODIA_TOTAL',
				p_TablaCarteras				  => :v_tablaClientes,
				p_TablaInstrumentos			  => :v_tablaInstrumentos,
				p_out_TablaSalida    		  => V_out_FlujoTotalizadoFinal
		);		
		
	
	
		SELECT COUNT(1) INTO v_Count FROM :V_out_FlujoTotalizadoFinal;

		IF v_Count > 0 THEN
		
--------------------
--CUSTODIA
--------------------
		v_tablaSalida=
			SELECT
			--GENERALES
			10 AS "OrdenInterno"
			,v_NombreDestinatario AS "NombreDestinatario"
			,v_Rut AS "Rut"
			,v_Direccion AS "Direccion"
			,v_Comuna AS "Comuna"
			,v_Ciudad AS "Ciudad"
			,p_FormaSalida AS "FormaSalida"
			,TO_VARCHAR(COALESCE(:p_FechaConsulta,''),'DD-MM-YYYY')  AS "FechaConsulta"
			,(COALESCE(CLA."Nombre",'') || ' - ' || COALESCE(F."IdMoneda",'')) AS "Grupo"
			--ESPECIFICOS
			,COALESCE(F."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
			,COALESCE(F."Nemotecnico",'') AS "Nemotecnico"
			,COALESCE(F."IdMoneda",'') AS "IdMoneda"
			,SUM(F."CantidadDisponible") AS "Cantidad"
			,SUM(F."MontoMercadoCustodia") AS "ValorPresente"
			,CASE F."FechaVencimiento" WHEN '0001-01-01' THEN ''
			 ELSE TO_VARCHAR(COALESCE(F."FechaVencimiento",''),'DD/MM/YYYY') END AS "FechaVencimiento"
			,F."PrecioMercadoCustodia" 		AS "PrecioCierre"
			,MAX(F."TasaMercadoActual") AS "Tasa"	
			,F."IdCliente"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			FROM :V_out_FlujoTotalizadoFinal F
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=F."IdClasificacionTipoInstrumento")
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
			ON (CP."IdPortafolio"=F."IdPortafolio")
			WHERE CLA."IdEstado"='ACT' AND F."TipoCartera"='CUSTODIA'
			GROUP BY F."IdCliente",F."IdClasificacionTipoInstrumento",CLA."Nombre",F."Nemotecnico",F."IdMoneda",F."PrecioMercadoCustodia",F."FechaVencimiento";

--------------------
--GARANTIA
--------------------
		v_tablaSalida=  SELECT * FROM :v_tablaSalida UNION ALL
			SELECT
			--GENERALES
			11 AS "OrdenInterno"
			,v_NombreDestinatario AS "NombreDestinatario"
			,v_Rut AS "Rut"
			,v_Direccion AS "Direccion"
			,v_Comuna AS "Comuna"
			,v_Ciudad AS "Ciudad"
			,p_FormaSalida AS "FormaSalida"
			,p_FechaConsulta AS "FechaConsulta"
			,'GARANTIA' AS "Grupo"
			--ESPECIFICOS
			,COALESCE(F."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
			,COALESCE(F."Nemotecnico",'') AS "Nemotecnico"
			,COALESCE(F."IdMoneda",'') AS "IdMoneda"
			,SUM(F."CantidadDisponible") AS "Cantidad"
			,SUM(F."MontoMercadoCustodia") AS "ValorPresente"
			,CASE F."FechaVencimiento" WHEN '0001-01-01' THEN ''
			 ELSE TO_VARCHAR(COALESCE(F."FechaVencimiento",''),'DD/MM/YYYY') END AS "FechaVencimiento"
			,F."PrecioMercadoCustodia" 		AS "PrecioCierre"
			,MAX(F."TasaMercadoActual") AS "Tasa"	
			,F."IdCliente"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			FROM :V_out_FlujoTotalizadoFinal F
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=F."IdClasificacionTipoInstrumento")
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
			ON (CP."IdPortafolio"=F."IdPortafolio")
			WHERE CLA."IdEstado"='ACT' AND F."TipoCartera"='GARANTIAS'
			GROUP BY F."IdCliente",F."IdClasificacionTipoInstrumento",CLA."Nombre",F."Nemotecnico",F."IdMoneda",F."PrecioMercadoCustodia",F."FechaVencimiento";


-----------------------
--OPERACIONES A PLAZO
-----------------------			
				
		  --Se obtienen operaciones segun Productos configurados para la Normativa
	
		  v_tablaProductos=
		 	SELECT   NPRO."IdTipoProducto"
		 			,NPRO."IdClasificacionTipoInstrumento"
				 	,NPRO."IdTipoInstrumento"
				 	,NPRO."IdTipoOperacion" 
				 	,NPRO."Uso" 
				 	
		 	FROM  "BSCL.Normativa.BD::NOR.Producto" NPRO 	--PRODUCTO NORMATIVA
			WHERE NPRO."IdNormativa"=:v_IdNormativa;
		
			SELECT COUNT(1) INTO v_Count FROM :v_tablaProductos;
			IF v_Count=0 THEN
				v_tablaProductos =SELECT
				'' AS "IdTipoProducto",
				'' AS "IdClasificacionTipoInstrumento",
				'' AS "IdTipoInstrumento", 
				'' AS "IdTipoOperacion",
				'' AS "Uso" 
				FROM DUMMY WHERE 1=2;	
			END IF;
			
		  --se obtienen parametros de operaciones 	
			
			V_tablaOperacionGrupo =
						SELECT 	 MAX( CASE WHEN SOL."Valor"='registroPACTOS' THEN 'S' ELSE NULL END) AS "PAC" --PACTO
								,MAX( CASE WHEN SOL."Valor"='registroSIMULTANEA' THEN 'S' ELSE NULL END) AS "SIM" --SIMULTANEA
								,MAX( CASE WHEN SOL."Valor"='registroVENTACORTA' THEN 'S' ELSE NULL END) AS "VTACOR" --VENTAS CORTAS
						FROM "BSCL.Normativa.BD::CIR.Parametro" AS SOL
						WHERE SOL."Periodo"=''	AND SOL."Codigo"='TIPOOPER' AND SOL."IdEstado"='ACT';
						
				SELECT TOP 1 "PAC","SIM","VTACOR"
					   INTO v_PAC,v_SIM ,v_VTACOR FROM :V_tablaOperacionGrupo;
			
			IF v_SIM='S' THEN --SIMULTANEA
			
				v_tablaSalida=
					SELECT
					--GENERALES
					20 AS "OrdenInterno"
					,v_NombreDestinatario AS "NombreDestinatario"
					,v_Rut AS "Rut"
					,v_Direccion AS "Direccion"
					,v_Comuna AS "Comuna"
					,v_Ciudad AS "Ciudad"
					,p_FormaSalida AS "FormaSalida"
					,p_FechaConsulta AS "FechaConsulta"
					--,(CLA."Nombre" || ' X ' ||OPE."IdMoneda") AS "Grupo"
					,'SIMULTANEA' || ' - ' || OPE."AccionOperacion" AS "Grupo"
					--ESPECIFICOS
					,COALESCE(OPE."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
					,COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico"
					,COALESCE(OPE."IdMoneda",'') AS "IdMoneda"  
					,SUM(OPE."Cantidad") AS "Cantidad"
					,SUM(OPE."ValorPresenteCompraInst") AS "ValorPresente"
					,TO_VARCHAR (COALESCE(OPE."FechaVencimiento",''), 'DD/MM/YYYY')	AS "FechaVencimiento"
					,OPE."Precio"		AS "PrecioCierre"
					,COALESCE(OPE."Tasa",0) AS "Tasa"
					,OPE."IdCliente"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
					FROM "BSCL.Facturacion.BD.Vistas::ConsultarOperaciones" OPE
					INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
					ON (CLA."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento")
					INNER JOIN :v_tablaProductos PRO
					ON ( (PRO."IdTipoProducto"=OPE."IdTipoProducto" OR PRO."IdTipoProducto" = '' ) 
		 				AND	(PRO."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento" OR PRO."IdClasificacionTipoInstrumento" = '')
				 		AND (PRO."IdTipoInstrumento"=OPE."IdTipoInstrumento" OR PRO."IdTipoInstrumento" = '')
				 		AND (PRO."IdTipoOperacion" =OPE."IdTipoOperacion" OR PRO."IdTipoOperacion" = '' ))
			 		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
					ON (CP."IdPortafolio"=OPE."IdPortafolio")
					WHERE CLA."IdEstado"='ACT'
					AND  OPE."IdCliente"=p_IdCliente
				    AND TO_DATE(OPE."FechaTransaccion") <=TO_DATE(p_FechaConsulta)
				    GROUP BY OPE."IdCliente",OPE."IdClasificacionTipoInstrumento",OPE."Nemotecnico",OPE."IdMoneda",OPE."Precio",OPE."FechaVencimiento",OPE."AccionOperacion",OPE."Tasa"
					UNION SELECT * FROM :v_tablaSalida;
			
			END IF; 
			
			IF v_PAC='S' THEN --PACTO
			
				v_tablaSalida=
					SELECT
					--GENERALES
					30 AS "OrdenInterno"
					,v_NombreDestinatario AS "NombreDestinatario"
					,v_Rut AS "Rut"
					,v_Direccion AS "Direccion"
					,v_Comuna AS "Comuna"
					,v_Ciudad AS "Ciudad"
					,p_FormaSalida AS "FormaSalida"
					,p_FechaConsulta AS "FechaConsulta"
					--,(CLA."Nombre" || ' X ' ||OPE."IdMoneda") AS "Grupo"
					,'PACTO' || ' - ' || OPE."AccionOperacion" AS "Grupo"
					--ESPECIFICOS
					,COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
					,COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico"
					,COALESCE(OPE."IdMoneda",'')  AS "IdMoneda"
					,SUM(OPE."Cantidad") AS "Cantidad" 
					,SUM(OPE."ValorPresenteCompraInst") AS "ValorPresente"
					,TO_VARCHAR (COALESCE(OPE."FechaVencimiento",''), 'DD/MM/YYYY')		AS "FechaVencimiento"
					,0 		AS "PrecioCierre"
					,COALESCE(OPE."Tasa",0) AS "Tasa"
					,OPE."IdCliente"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
					FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperaciones" OPE
					INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
					ON (CLA."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento")
					INNER JOIN :v_tablaProductos PRO
					ON ( (PRO."IdTipoProducto"=OPE."IdTipoProducto" OR PRO."IdTipoProducto" = '' ) 
		 				AND	(PRO."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento" OR PRO."IdClasificacionTipoInstrumento" = '')
				 		AND (PRO."IdTipoInstrumento"=OPE."IdTipoInstrumento" OR PRO."IdTipoInstrumento" = '')
				 		AND (PRO."IdTipoOperacion" =OPE."IdTipoOperacion" OR PRO."IdTipoOperacion" = '' ))
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
					ON (CP."IdPortafolio"=OPE."IdPortafolio")
					WHERE CLA."IdEstado"='ACT'
					AND  OPE."IdCliente"=p_IdCliente
				   AND TO_DATE(OPE."FechaMovimiento") <=TO_DATE(p_FechaConsulta)
				   GROUP BY OPE."IdCliente",OPE."IdClasificacionTipoInstrumento",OPE."Nemotecnico",OPE."IdMoneda",OPE."Precio",OPE."FechaVencimiento",OPE."AccionOperacion",OPE."Tasa"
					UNION SELECT * FROM :v_tablaSalida;
			
			END IF; 

			
			
				IF v_VTACOR='S' THEN --VENTAS CORTAS
			
				v_tablaSalida=
					SELECT
					--GENERALES
					40 AS "OrdenInterno"
					,v_NombreDestinatario AS "NombreDestinatario"
					,v_Rut AS "Rut"
					,v_Direccion AS "Direccion"
					,v_Comuna AS "Comuna"
					,v_Ciudad AS "Ciudad"
					,p_FormaSalida AS "FormaSalida"
					,p_FechaConsulta AS "FechaConsulta"
					--,(CLA."Nombre" || ' X ' ||OPE."IdMoneda") AS "Grupo"
					,'VENTA CORTA' AS "Grupo"
					--ESPECIFICOS
					,COALESCE(OPE."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
					,COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico"
					,COALESCE(OPE."IdMoneda",'') AS "IdMoneda"  
					,SUM(OPE."Cantidad") AS "Cantidad"
					,SUM(OPE."ValorPresenteCompraInst") AS "ValorPresente"
					,TO_VARCHAR (COALESCE(OPE."FechaVencimiento",''), 'DD/MM/YYYY')		AS "FechaVencimiento"
					,OPE."Precio"		AS "PrecioCierre"
					,COALESCE(OPE."Tasa",0) AS "Tasa"
					,OPE."IdCliente"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
					FROM "BSCL.Facturacion.BD.Vistas::ConsultarOperaciones" OPE
					INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
					ON (CLA."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento")
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
					ON (CP."IdPortafolio"=OPE."IdPortafolio")
					/*
					INNER JOIN :v_tablaProductos PRO
					ON ( (PRO."IdTipoProducto"=OPE."IdTipoProducto" OR PRO."IdTipoProducto" = '' ) 
		 				AND	(PRO."IdClasificacionTipoInstrumento"=OPE."IdClasificacionTipoInstrumento" OR PRO."IdClasificacionTipoInstrumento" = '')
				 		AND (PRO."IdTipoInstrumento"=OPE."IdTipoInstrumento" OR PRO."IdTipoInstrumento" = '')
				 		AND (PRO."IdTipoOperacion" =OPE."IdTipoOperacion" OR PRO."IdTipoOperacion" = '' )  AND  PRO."Uso"='VENTACORTA')
				 	*/	
				 		
					WHERE CLA."IdEstado"='ACT'
					AND  OPE."IdCliente"=p_IdCliente
				    AND TO_DATE(OPE."FechaTransaccion") <=TO_DATE(p_FechaConsulta)
				    AND OPE."EsVentaCorta"='S'
				    GROUP BY OPE."IdCliente",OPE."IdClasificacionTipoInstrumento",OPE."Nemotecnico",OPE."IdMoneda",OPE."Precio",OPE."FechaVencimiento",OPE."ValorPresenteCompraInst",OPE."Tasa"
					UNION SELECT * FROM :v_tablaSalida;
			
			END IF;
		ELSE
		v_tablaSalida=
			SELECT
			0 AS "OrdenInterno"
			,v_NombreDestinatario AS "NombreDestinatario"
			,v_Rut AS "Rut"
			,v_Direccion AS "Direccion"
			,v_Comuna AS "Comuna"
			,v_Ciudad AS "Ciudad"
			,p_FormaSalida AS "FormaSalida"
			,p_FechaConsulta AS "FechaConsulta"
			,'' AS "Grupo"
			,'' AS "IdClasificacionTipoInstrumento"
			,'' AS "Nemotecnico"
			,'' AS "IdMoneda" 
			,0 AS "Cantidad"
			,0 AS "ValorPresente"
			,'' 	AS "FechaVencimiento"
			,0 		AS "PrecioCierre"
			,0 AS "Tasa"
			,"IdCliente" AS "IdCliente"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			FROM :v_tablaClientes;--DUMMY;-- WHERE 1=2;
		END IF;
		
		--verificamos si se debe agregar cuentacaja al reporte
		SELECT "Valor" INTO v_AgregarCuentaCaja
		FROM "BSCL.Normativa.BD::NOR.Configuracion"
		WHERE "CodigoParametro" = 'AGREGAR_CUENTA_CAJA_CIRCULARIZACION';
		
		IF v_AgregarCuentaCaja = 'S' THEN
		
			v_CuentaInversionAConsultar = SELECT NULL AS "IdEjecutivo",
			"IdPortafolio" as "IdPortafolio", NULL AS "IdMoneda" FROM :v_tablaClientes;
			
			CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionConsolidada"
				(p_IdEmpresa => p_IdEmpresa,
				p_IdUsuario => p_IdUsuario, 
				p_IdModulo => 'CTA',
				p_UsaReglasNegocio => 'S',
				p_CuentaInversionAConsultar => :v_CuentaInversionAConsultar,
				p_IdCliente => p_IdCliente,
				p_IdPortafolio => null,
				p_IdProductoAgrupador => null,
				p_FechaDesde => p_FechaConsulta,
				p_FechaHasta => p_FechaConsulta,
				p_IncluyeOtrosSaldos => 'S',
				p_IncluyeProductoApv => 'S',
				p_out_SaldoInicialFinal => v_SaldoInicialFinal,
				p_out_SaldoPorFechaEfectiva => v_SaldoPorFechaEfectiva,
				p_out_SaldoPorFechaMovimiento => v_SaldoPorFechaMovimiento,
				p_out_InformacionContador => v_InformacionContador,
				p_out_InformacionDetalle => v_InformacionDetalle,
				p_out_AdvertenciasContador => v_AdvertenciasContador,
				p_out_AdvertenciasDetalle => v_AdvertenciasDetalle,
				p_out_ErroresContador => v_ErroresContador,
				p_out_ErroresDetalle => v_ErroresDetalle);
			
			--IF v_InformacionContador > 0 THEN
				v_tablaSalida=  SELECT
				50 AS "OrdenInterno"
				,v_NombreDestinatario AS "NombreDestinatario"
				,v_Rut AS "Rut"
				,v_Direccion AS "Direccion"
				,v_Comuna AS "Comuna"
				,v_Ciudad AS "Ciudad"
				,p_FormaSalida AS "FormaSalida"
				,p_FechaConsulta AS "FechaConsulta"
				,'CUENTA CAJA' AS "Grupo"
				--ESPECIFICOS
				,COALESCE(F."NombreMoneda",'') AS "IdClasificacionTipoInstrumento"
				,COALESCE(F."NombreMoneda",'') AS "Nemotecnico"
				,COALESCE(F."IdMoneda",'') AS "IdMoneda"
				,0 AS "Cantidad"
				,SUM(F."SaldoDisponible") AS "ValorPresente"
				,'' AS "FechaVencimiento"
				,0 AS "PrecioCierre"
				,0 AS "Tasa"	
				,p_IdCliente AS "IdCliente"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
				FROM :v_SaldoPorFechaMovimiento F
				INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
				ON (CP."IdPortafolio"=F."IdPortafolio")
				INNER JOIN :v_tablaClientes TC
				ON (CP."IdPortafolio"=TC."IdPortafolio")
				GROUP BY F."NombreMoneda",F."IdMoneda"
				UNION SELECT * FROM :v_tablaSalida;
			--END IF;
		END IF;

	SELECT * FROM :v_tablaSalida
	CROSS JOIN :v_datosGenerales
	CROSS JOIN :v_logo ORDER BY "OrdenInterno","Grupo";	
END; 
