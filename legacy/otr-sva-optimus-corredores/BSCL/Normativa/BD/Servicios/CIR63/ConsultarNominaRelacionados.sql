SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR63::ConsultarNominaRelacionados";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar información para emitir la Nomina de Relacionados.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 05/08/2016

"BSCL.Normativa.BD.Servicios.CIR63::ConsultarNominaRelacionados" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_TipoInformacion						NVARCHAR(3)	DEFAULT 'AM',--(RE)RELACIONADO, (IP)INFORMACION PRIVILEGIADA,(AM)AMBOS


	IN	p_IdCliente 							BIGINT 			DEFAULT '0',

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaNomina	"BSCL.Normativa.BD::NOR.SALIDA_CIR63NOMINA" 

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='CIR63';
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';				
		DECLARE v_IdSinRelacion								NVARCHAR(6):='SINREL';
		DECLARE v_DescripcionSinRelacion					NVARCHAR(520):='';
		
		DECLARE v_ValorLimite								DECIMAL (26,8):=0;		
		DECLARE v_IdPersonaCtaPropia,v_Count 				BIGINT:=0;
		DECLARE v_NombreCtaPropia							NVARCHAR(100):='';

		DECLARE v_out_error			  			 			NVARCHAR(500);
		DECLARE v_Periodo			  			 			NVARCHAR(7);
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		---Parametros
		
		---Periodo 
		SELECT TOP 1 LEFT("Valor",7) INTO v_Periodo
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro"='PLA.FechaSistema' and "IdEmpresa"=p_IdEmpresa;
		
		--Valores de la cuenta propia	
		SELECT TOP 1 COALESCE(SOC."IdPersona",0),COALESCE(RPAD(PER."NombreCompleto",100),'') INTO v_IdPersonaCtaPropia,v_NombreCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	     				ON (SOC."IdPersona"=PER."IdPersona")
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';
		
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		
		--Destinatario

		SELECT "NombreDestinatario", "CargoDestinatario" INTO v_NombreDestinatario, v_CargoDestinatario
		FROM "BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"=v_IdNormativa;
		
		--Homologo Tipo Sin Relacion
		
		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "IdTipoRelacion"=v_IdSinRelacion AND "Administracion"='SISTEMA';
		
		IF v_Count>0 THEN
			SELECT "NombreTipoRelacion" INTO v_DescripcionSinRelacion
				FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "IdTipoRelacion"=v_IdSinRelacion AND "Administracion"='SISTEMA';
		END IF;
		

		v_tablaClientes=
		SELECT
			CLI."IdPersona",
			CLI."IdCliente",
			--IFNULL(REL."IdTipoRelacion",v_IdSinRelacion)				AS "IdTipoRelacion",
			--IFNULL(REL."NombreTipoRelacion",v_DescripcionSinRelacion)	AS "NombreTipoRelacion"
			IFNULL(REL."CodigoValorIndicador",v_IdSinRelacion)				AS "IdTipoRelacion",
			IFNULL(REL."NombreValorIndicador",v_DescripcionSinRelacion)	AS "NombreTipoRelacion"
			
		FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
					ON (SOC."IdCliente" =CLI."IdCliente")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
						ON ( IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
							AND IND."IdCliente" =SOC."IdCliente")
					 --RELACIONADOS CON LA CORREDORA
					 /*
		LEFT JOIN (
					SELECT PER."IdPersonaDestino",PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
					FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
					WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
					AND PER."IdEstado"='ACT'
					) REL 
					ON (REL."IdPersonaDestino"=SOC."IdPersona")
					
					*/
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='NCG69PROP' AND  REL."CodigoValorIndicador" <>''
							AND REL."IdCliente" =SOC."IdCliente")			
		WHERE 
		 (CLI."IdCliente" = p_IdCliente OR p_IdCliente= 0)
 		AND (IND."IdPersona" IS NOT NULL  OR p_TipoInformacion='RE')
 		AND (REL."CodigoValorIndicador" IS NOT NULL OR p_TipoInformacion='IP');
 		--AND (REL."IdPersonaDestino" IS NOT NULL  OR p_TipoInformacion='IP');
	
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_tablaClientes;
 				 
		IF v_Count > 0 THEN

		--Tabla de Salida


			p_out_TablaNomina=
			SELECT 
				v_NombreDestinatario AS "NombreDestinatario",
				v_CargoDestinatario AS "CargoDestinatario",
				OPE."Direccion",
				OPE."Comuna",
				OPE."Ciudad",
 				OPE."IdPersona",
				OPE."NombrePersona",
  				(CASE WHEN COALESCE(TREL."IdTipoRelacionDestino",'') ='CEMDOR'  THEN v_IdPersonaCtaPropia ELSE OPE."IdPersonaEmpleador" END) AS "IdPersonaEmpleador" ,
  				(CASE WHEN COALESCE(TREL."IdTipoRelacionDestino",'') ='CEMDOR'  THEN v_NombreCtaPropia ELSE OPE."NombreEmpleador" END) AS "NombreEmpleador" ,
				OPE."DocumentoIdentidad",
				OPE."TipoDocumentoIdentidad",
				OPE."NombreAbrTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidad",
				OPE."IdTipoRelacion",
				OPE."NombreTipoRelacion",
				OPE."IdTipoRelacion" AS "CodigoRelacionNorma",
				OPE."NombreTipoRelacion" AS "DescripcionRelacionNorma",
				TO_VARCHAR(v_Periodo,'MM-YYYY') AS "Periodo",
				L."LogoCorredora"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
				,OPE."Cargo"
				,OPE."CodigoCargo"
			FROM	
			(			
	     			SELECT 
						CONCAT(CONCAT(COALESCE("DIR"."Calle", ''), ' '), COALESCE(CAST("DIR"."Numero" AS VARCHAR), '')) AS "Direccion",
						COALESCE(DIR."NombreDivisionPais",'')	AS "Comuna",
						COALESCE(DIR."NombreCiudad",'')			AS "Ciudad",
						PER."IdPersona",
						PER."NombreCompleto" 			AS "NombrePersona", 
						PER."IdPersonaEmpleador",
						COALESCE(EMP."RazonSocial",'') 			AS "NombreEmpleador",
						PER."DocumentoIdentidad",
						PER."TipoDocumentoIdentidad",
						TIDEN."NombreAbrTipoDocumentoIdentidad",
						OP."IdTipoRelacion",
						OP."NombreTipoRelacion",
						TCE."NombreTipoCargoEmpleado"  AS "Cargo",
						TCE."IdTipoCargoEmpleado" AS "CodigoCargo"
	     			FROM :v_tablaClientes OP
	     			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	     				ON (OP."IdPersona"=PER."IdPersona")
	     			INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN 
	     				ON  (PER."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" )
	     			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR
	     				ON (DIR."IdDireccionFisica"=PER."IdDireccionFisicaPrincipal")
	     			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" EMP
	     				ON (EMP."IdPersona"=PER."IdPersonaEmpleador")
					LEFT OUTER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" PEMP 
	     				ON (PER."IdPersona" = PEMP."IdPersona" )
					LEFT OUTER JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" TCE 
						ON (TCE."CodigoTipoCargoEmpleado" = PEMP."CodigoTipoCargoEmpleado")
					--ORDER BY OP."IdCliente",OP."FechaOperacion",OP."Nemotecnico",OP."IdTipoOperacion" 
			) OPE 
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TREL 
			ON (TREL."IdTipoRelacion"=OPE."IdTipoRelacion") CROSS JOIN :v_logo L;
			--LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa" NOR
			--ON (OPE."IdTipoRelacion"=NOR."IdTipoRelacion" AND NOR."IdNormativa"=v_IdNormativa);
				
				
		ELSE
		p_out_TablaNomina=SELECT
								'' AS	 "NombreDestinatario",
								'' AS 	 "CargoDestinatario",
								'' AS	 "Direccion",
								'' AS	 "Comuna",
								'' AS	 "Ciudad",
 								0 AS	 "IdPersona",
								'' AS	 "NombrePersona",
  								0 AS	 "IdPersonaEmpleador",
 								'' AS	 "NombreEmpleador",
								'' AS	 "DocumentoIdentidad",
								'' AS	 "TipoDocumentoIdentidad",
								'' AS	 "NombreTipoDocumentoIdentidad",
								'' AS	 "IdTipoRelacion",
								'' AS	 "NombreTipoRelacion",
								'' AS	 "CodigoRelacionNorma",
								'' AS	 "DescripcionRelacionNorma",
								'' AS 	 "Periodo",
								L."LogoCorredora"
								,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
								,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
								,'' AS "Cargo"
								,0 AS "CodigoCargo"
				FROM DUMMY  CROSS JOIN :v_logo L WHERE 1=2;	
		
		END IF;
		SELECT * FROM :p_out_TablaNomina;		

END; 