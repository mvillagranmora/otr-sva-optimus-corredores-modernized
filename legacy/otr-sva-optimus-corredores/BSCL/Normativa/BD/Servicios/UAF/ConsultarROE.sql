SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::ConsultarROE";
 GO
CREATE PROCEDURE 
--
--  Proposito:  Servicio para consultar información para emitir Transacciones con Declaracion Origen de Fondos
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07/12/2016

"BSCL.Normativa.BD.Servicios.UAF::ConsultarROE" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    --VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_Monto		 							DECIMAL(27,8)	DEFAULT 0,
    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaROE	"BSCL.Normativa.BD::NOR.SALIDA_UAFROE"
	
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
		DECLARE v_Count,v_IdTrabajoParidad			 		 BIGINT:=0;
		DECLARE v_IdNormativa 						 		 NVARCHAR(15):='UAF';
		DECLARE v_IdTipoDocumento   		 				 NVARCHAR(10):='UAFROE';
		DECLARE v_VAL_MONTO_UAFROE, v_MON_MONTO_UAFROE 	 	 NVARCHAR(520):='';
		DECLARE v_ValorLimite								 DECIMAL (26,8):=0;	
		DECLARE v_out_error			  						 NVARCHAR(500);
    	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
    	DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
    	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		---Parametros
		v_FechaOperacionDesde:=p_FechaOperacionDesde;
  		v_FechaOperacionHasta:=p_FechaOperacionHasta;  		
  		
		IF v_FechaOperacionDesde='' AND v_FechaOperacionHasta='' THEN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			v_FechaOperacionDesde=TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD');
			v_FechaOperacionHasta=v_FechaOperacionDesde;
		END IF;

		IF v_FechaOperacionHasta='' THEN
			v_FechaOperacionHasta:=v_FechaOperacionDesde;  		
		END IF;
		
		IF v_FechaOperacionDesde='' THEN
			v_FechaOperacionDesde:=v_FechaOperacionHasta;  		
		END IF;
		
		
		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UAFROE' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN
			SELECT "Valor" INTO v_VAL_MONTO_UAFROE
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UAFROE' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_VAL_MONTO_UAFROE:='10000';
		END IF;
		IF p_Monto   > 0  THEN
			v_VAL_MONTO_UAFROE = CAST (p_Monto AS NVARCHAR(520));
		END IF;

		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='MON_MONTO_UAFROE' AND "IdNormativa"=v_IdNormativa;
		IF v_Count>0 THEN	
			SELECT trim("Valor") INTO v_MON_MONTO_UAFROE
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='MON_MONTO_UAFROE' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_MON_MONTO_UAFROE:= 'USD';			

		END IF;
		v_ValorLimite=	CAST(TRIM(v_VAL_MONTO_UAFROE) AS DECIMAL (26,8));
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
--- DATOS SUCURSAL
		 COALESCE(SUC."IdClasificacionDivisionPais",0) 	AS "RegionSucursal"
		,COALESCE(SUC."IdDivisionPais",0) 				AS "LocalidadSucursal"
		,COALESCE(SUC."Calle",'') 						AS "CalleSucursal"
		,COALESCE(SUC."Numero",'') 						AS "NumeroSucursal"
		,COALESCE(SUC."Depto",'') 						AS "DeptoSucursal"
		,COALESCE(SUC."Telefono",'') 					AS "TelefonoSucursal"		
--- DATOS OPERACION
		, TES."IdComprobante"
		, TES."FechaLiquidacion"
		, TES."IdPersona"
		, TES."IdPersonaTitularPago"
---ENTRANTE
		--, TES."NombreMoneda"
		--, (CASE  WHEN TES."IdTipoMovimiento"='MOVING' THEN TES."IdMoneda" ELSE '0' END ) AS  "IdMonedaEntrante"
		, (CASE  WHEN TES."IdTipoMovimiento"='MOVING' THEN TES."Monto" 	  ELSE  0  END ) AS  "MontoEntrante"
		,  '' 												AS "DenBilleteEntrante"
---SALIENTE
		--, (CASE  WHEN TES."IdTipoMovimiento"='MOVEGR' THEN TES."IdMoneda" ELSE '0' END ) AS  "IdMonedaSaliente"
		, (CASE  WHEN TES."IdTipoMovimiento"='MOVEGR' THEN TES."Monto" 	  ELSE  0  END ) AS  "MontoSaliente"		
		,  '' 												AS "DenBilleteSaliente"
----CLIENTE		
		, PERC."DocumentoIdentidad" 						AS "Identificacion"
		, PERC."TipoDocumentoIdentidad"		 				AS "TipoDocumento"
		, PERC."Tipo"		 								AS "TipoPersona"
		, PERC."RazonSocial"								AS "RazonSocial"
		, PERC."ApellidoPaterno" 							AS "ApellidoPaterno"
		, PERC."ApellidoMaterno" 							AS "ApellidoMaterno"
		, COALESCE(PERC."IdPaisNacionalidadPrincipal",0) 	AS "Nacionalidad"
		, ''   												AS "Habitualidad"
		, PERC."IdPaisNacimiento" 							AS "Pais"
		, ''   												AS "Region"
		, COALESCE(DIRC."NombreDivisionPais",'')  			AS "Comuna"
		, COALESCE(DIRC."Calle",'')  						AS "Calle"
		, COALESCE(DIRC."Numero",'')  						AS "Numero"
		, COALESCE(DIRC."Departamento",'')  				AS "Departamento"
		, COALESCE(TELC."Numero",'') 						AS "Telefono"
		---REPRESENTANTE PAGADOR
		, PERR."DocumentoIdentidad"							AS "RepIdentificacion"
		, PERR."TipoDocumentoIdentidad"		 				AS "RepTipoDocumento"
		, PERR."Tipo"		 								AS "RepTipoPersona"
		, PERR."RazonSocial"								AS "RepRazonSocial"
		, PERR."ApellidoPaterno"							AS "RepApellidoPaterno"
		, PERR."ApellidoMaterno"							AS "RepApellidoMaterno"
		, COALESCE(PERR."IdPaisNacionalidadPrincipal",0) 	AS "RepNacionalidad"
		, ''   												AS "RepHabitualidad"
		, PERR."IdPaisNacimiento" 							AS "RepPais"
		, ''   												AS "RepRegion"
		, COALESCE(DIRR."NombreDivisionPais",'')  			AS "RepComuna"
		, COALESCE(DIRR."Calle",'')  						AS "RepCalle"
		, COALESCE(DIRR."Numero",'')  						AS "RepNumero"
		, COALESCE(DIRR."Departamento",'') 					AS "RepDepartamento"
		, COALESCE(TELR."Numero",'') 						AS "RepTelefono"
		, TES."IdTipoMovimiento"							AS "IdTipoMovimiento"
		, TES."IdMoneda" 									AS "IdMoneda"
		, TES."Monto"										AS "Monto"
		, TES."IdComprobanteFormaDePago"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" TES
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERC
		ON (TES."IdPersona"=PERC."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELC
		ON (TELC."IdTelefono"=PERC."IdTelefonoPrincipal")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIRC
		ON (PERC."IdDireccionFisicaPrincipal"=DIRC."IdDireccionFisica") 
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERR
		ON (TES."IdPersonaTitularPago"=PERR."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELR
		ON (TELR."IdTelefono"=PERR."IdTelefonoPrincipal")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIRR
		ON (PERR."IdDireccionFisicaPrincipal"=DIRR."IdDireccionFisica") 	
		---BUSCAR LA SUCURSAL DE INGRESO
	
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" INGEGR
		ON (INGEGR."IdComprobante"=TES."IdComprobante")
		/*
		 --comentado mientras tesoreria no tenga la sucursal de digitacion 
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC
		ON (INGEGR."IdSucursalEmpleadoDigitador"=SUC."IdSucursal")
		*/
		
	     ---sucursal directamenet desde el emplado
	    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  EMP
		ON (EMP."IdEmpleado"=INGEGR."IdEmpleadoDigitador")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC
		ON (EMP."IdSucursal"=SUC."IdSucursal")
	  
		WHERE TES."IdMedioDePago" = 'EFE' AND TES."IdEstado"='LIQ' AND TES."IdEmpresa" =p_IdEmpresa
		AND TO_DATE(TES."FechaLiquidacion") BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta);

	
	
	
	
			 
		--Verificar si existen operaciones 
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN

			---IGUALAR PARIDADES			
										
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdComprobanteFormaDePago" AS "IdInterno",TO_VARCHAR (CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", v_MON_MONTO_UAFROE as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR;
			
			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
				
			
			
			v_out_ParidadDisponibles=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdInterno",outt."IdMonedaDestino", par.*
					FROM :v_tablaOperaciones par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdComprobanteFormaDePago" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			
			
											
			--Igualar montos de Paridades por cliente (no portafolio) e identifica aquella transacciones que sumadas alcanzan las 500 UF (limite parametro)
			v_MontoOperacionParidad=
				SELECT DISTINCT PAR."MontoDestino" AS "Monto", CAR."IdPersona",CAR."IdComprobanteFormaDePago",PAR."IdMonedaDestino"
				FROM :v_tablaOperaciones CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
				ON (CAR."IdComprobanteFormaDePago"=PAR."IdInterno")
			WHERE PAR."MontoDestino" >= v_ValorLimite;
			--Identifica los clientes cuyas operaciones  alcanzan las 450 UF (limite parametro)

						
		--Tabla de Salida
			p_out_TablaROE=
			SELECT 
				OPE."RegionSucursal",
				OPE."LocalidadSucursal",
				OPE."CalleSucursal",
				OPE."NumeroSucursal",
				OPE."DeptoSucursal",
				OPE."TelefonoSucursal",
				--- DATOS OPERACION
				OPE."IdComprobante",
				OPE."FechaLiquidacion",
				OPE."IdPersona",
				OPE."IdPersonaTitularPago",
				---ENTRANTE
				OPE."IdMonedaEntrante",
				OPE."MontoEntrante",
				OPE."DenBilleteEntrante",
				---SALIENTE
				OPE."IdMonedaSaliente",
				OPE."MontoSaliente",
				OPE."DenBilleteSaliente",
				OPE."IdComprobante" AS "NumComprobante",
				OPE."FechaLiquidacion" AS "Fecha",	
				----CLIENTE		
				OPE."Identificacion",
				OPE."TipoDocumento",
				OPE."RazonSocial",
				OPE."ApellidoPaterno",
				OPE."ApellidoMaterno",
				OPE."Nacionalidad",
				OPE."Habitualidad",
				OPE."Pais",
				OPE."Region",
				OPE."Comuna",
				OPE."Calle",
				OPE."Numero",
				OPE."Departamento",
				OPE."Telefono",
				---REPRESENTANTE PAGADOR
				OPE."RepIdentificacion",
				OPE."RepTipoDocumento",
				OPE."RepRazonSocial",
				OPE."RepApellidoPaterno",
				OPE."RepApellidoMaterno",
				OPE."RepNacionalidad",
				OPE."RepHabitualidad",
				OPE."RepPais",
				OPE."RepRegion",
				OPE."RepComuna",
				OPE."RepCalle",
				OPE."RepNumero",
				OPE."RepDepartamento",
				OPE."RepTelefono"
				--OPE."IdTipoMovimiento",
				--OPE."IdMoneda",
				--OPE."Monto"
			FROM	
			(			
	     			SELECT 
	     			 	COALESCE(REGS."ValorDestino",'0') AS "RegionSucursal",
						COALESCE(COMS."ValorDestino",'0') AS "LocalidadSucursal",
						OP."CalleSucursal",
						OP."NumeroSucursal",
						OP."DeptoSucursal",
						OP."TelefonoSucursal",
						--- DATOS OPERACION
						OP."IdComprobante",
						OP."FechaLiquidacion",
						OP."IdPersona",
						OP."IdPersonaTitularPago",
						---ENTRANTE
						(CASE  WHEN OP."IdTipoMovimiento"='MOVING' THEN  CAST (COALESCE(MON."ValorDestino",'0') AS VARCHAR(3)) ELSE '0' END ) AS  "IdMonedaEntrante",
						OP."MontoEntrante",
						OP."DenBilleteEntrante",
						---SALIENTE
						(CASE  WHEN OP."IdTipoMovimiento"='MOVEGR' THEN  CAST (COALESCE(MON."ValorDestino",'0') AS VARCHAR(3)) ELSE '0' END ) AS  "IdMonedaSaliente",
						OP."MontoSaliente",
						--PA."Monto" AS "MontoSaliente",
						OP."DenBilleteSaliente",
						----CLIENTE		
						OP."Identificacion",
						(CASE OP."TipoDocumento" WHEN 'P' THEN '1'
												 WHEN 'R' THEN (CASE OP."TipoPersona" WHEN 'J' THEN '5'
												 				 ELSE '4'
												 				 END)
						 ELSE OP."TipoDocumento" 
						 END						
						) AS "TipoDocumento",
						OP."RazonSocial",
						OP."ApellidoPaterno",
						OP."ApellidoMaterno",
						COALESCE(NAC."ValorDestino",'0') AS "Nacionalidad",
						OP."Habitualidad",
						COALESCE(PAIS."ValorDestino",'0') AS "Pais",
						COALESCE(REG."ValorDestino",'0') AS "Region",
						COALESCE(COM."ValorDestino",'0') AS"Comuna",
						OP."Calle",
						OP."Numero",
						OP."Departamento",
						OP."Telefono",
						---REPRESENTANTE PAGADOR
						OP."RepIdentificacion",
						(CASE OP."RepTipoDocumento" WHEN 'P' THEN '1'
												    WHEN 'R' THEN (CASE OP."RepTipoPersona" WHEN 'J' THEN '5'
												 				 ELSE '4'
												 				 END)
						 ELSE OP."RepTipoDocumento" 
						 END) AS "RepTipoDocumento",
						OP."RepRazonSocial",
						OP."RepApellidoPaterno",
						OP."RepApellidoMaterno",
						COALESCE(NACR."ValorDestino",'0') AS "RepNacionalidad",
						OP."RepHabitualidad",
						COALESCE(PAISR."ValorDestino",'0') AS "RepPais",
						COALESCE(REGR."ValorDestino",'0') AS "RepRegion",
						COALESCE(COMR."ValorDestino",'0') AS "RepComuna",
						OP."RepCalle",
						OP."RepNumero",
						OP."RepDepartamento",
						OP."RepTelefono",
						OP."IdTipoMovimiento",
						OP."IdMoneda",
						OP."Monto"
	     			FROM :v_tablaOperaciones OP 
	     			INNER JOIN :v_MontoOperacionParidad PA
	     				ON (OP."IdPersona" =PA."IdPersona"AND OP."IdComprobanteFormaDePago"=PA."IdComprobanteFormaDePago")
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" MON ---MONEDAS
	     				ON (CAST (OP."IdMoneda" AS  nvarchar(100))= MON."ValorOrigen" AND MON."Tipo"='Moneda' AND MON."Esquema"=v_Esquema AND MON."Version"=v_Version)
	     				--Homologacion Cliente
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" PAIS ---PAIS
	     				ON (CAST (OP."Pais" AS  nvarchar(100))= PAIS."ValorOrigen" AND PAIS."Tipo"='Pais' AND PAIS."Esquema"=v_Esquema AND PAIS."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" NAC ---NACIONALIDAD
	     				ON (CAST (OP."Nacionalidad" AS  nvarchar(100))= NAC."ValorOrigen" AND NAC."Tipo"='Pais' AND NAC."Esquema"=v_Esquema AND NAC."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" REG ---REGION
	     				ON (CAST (OP."Region" AS  nvarchar(100))= REG."ValorOrigen" AND REG."Tipo"='ClasificacionDivisionPais' AND REG."Esquema"=v_Esquema AND REG."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" COM ---COMUNA
	     				ON (CAST (OP."Comuna" AS  nvarchar(100))= COM."ValorOrigen" AND COM."Tipo"='DivisionPais' AND COM."Esquema"=v_Esquema AND COM."Version"=v_Version)
	     				--Homologacion Representante
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" PAISR ---PAIS
	     				ON (CAST (OP."RepPais" AS  nvarchar(100))= PAISR."ValorOrigen" AND PAISR."Tipo"='Pais' AND PAISR."Esquema"=v_Esquema AND PAISR."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" NACR ---NACIONALIDAD
	     				ON (CAST (OP."RepNacionalidad" AS  nvarchar(100))= NACR."ValorOrigen" AND NACR."Tipo"='Pais' AND NACR."Esquema"=v_Esquema AND NACR."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" REGR ---REGION
	     				ON (CAST (OP."RepRegion" AS  nvarchar(100))= REGR."ValorOrigen" AND REGR."Tipo"='ClasificacionDivisionPais' AND REGR."Esquema"=v_Esquema AND REGR."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" COMR ---COMUNA
	     				ON (CAST (OP."RepComuna" AS  nvarchar(100))= COMR."ValorOrigen" AND COMR."Tipo"='DivisionPais' AND COMR."Esquema"=v_Esquema AND COMR."Version"=v_Version)
	     				--Homologacion Sucursal
		     			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" REGS ---REGION
	     				ON (CAST (OP."RegionSucursal" AS  nvarchar(100))= REGS."ValorOrigen" AND REGS."Tipo"='ClasificacionDivisionPais' AND REGS."Esquema"=v_Esquema AND REGS."Version"=v_Version)
	     				LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" COMS ---COMUNA
	     				ON (CAST (OP."LocalidadSucursal" AS  nvarchar(100))= COMS."ValorOrigen" AND COMS."Tipo"='DivisionPais' AND COMS."Esquema"=v_Esquema AND COMS."Version"=v_Version)
			) OPE ;
				
		ELSE

		
		p_out_TablaROE=SELECT
				--- DATOS SUCURSAL
				 '' AS "RegionSucursal",
				'' AS "LocalidadSucursal",
				'' AS "CalleSucursal",
				'' AS "NumeroSucursal",
				'' AS "DeptoSucursal",
				'' AS "TelefonoSucursal",
				--- DATOS OPERACION
				0 AS "IdComprobante",
				'' AS "FechaLiquidacion",
				0 AS "IdPersona",
				0 AS "IdPersonaTitularPago",
				---ENTRANTE
				'' AS "IdMonedaEntrante",
				0 AS "MontoEntrante",
				'' AS "DenBilleteEntrante",
				---SALIENTE
				'' AS "IdMonedaSaliente",
				0 AS "MontoSaliente",
				'' AS "DenBilleteSaliente",
				0 AS "NumComprobante",
				'' AS "Fecha",
				----CLIENTE		
				'' AS "Identificacion",
				'' AS "TipoDocumento",
				'' AS "RazonSocial",
				'' AS "ApellidoPaterno",
				'' AS "ApellidoMaterno",
				'' AS "Nacionalidad",
				'' AS "Habitualidad",
				'' AS "Pais",
				'' AS "Region",
				'' AS "Comuna",
				'' AS "Calle",
				'' AS "Numero",
				'' AS "Departamento",
				'' AS "Telefono",
				---REPRESENTANTE PAGADOR
				'' AS "RepIdentificacion",
				'' AS "RepTipoDocumento",
				'' AS "RepRazonSocial",
				'' AS "RepApellidoPaterno",
				'' AS "RepApellidoMaterno",
				'' AS "RepNacionalidad",
				'' AS "RepHabitualidad",
				'' AS "RepPais",
				'' AS "RepRegion",
				'' AS "RepComuna",
				'' AS "RepCalle",
				'' AS "RepNumero",
				'' AS "RepDepartamento",
				'' AS "RepTelefono"
				--'' AS "IdTipoMovimiento",
				--'' AS "IdMoneda",
				--0 AS "Monto"	
				FROM DUMMY WHERE 1=2;	
		
		END IF;
		SELECT * FROM :p_out_TablaROE;
END; 