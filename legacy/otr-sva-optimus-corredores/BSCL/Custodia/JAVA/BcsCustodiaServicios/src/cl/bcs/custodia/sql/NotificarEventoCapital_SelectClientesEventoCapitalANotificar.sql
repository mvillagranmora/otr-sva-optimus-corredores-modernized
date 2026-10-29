SELECT 
    --cabecera
    EC."IdEventoCapital",
    ECC."IdEventoCapitalCliente" 													AS "IdSecuencia",
	CCL."NombreCompleto"														AS "Nombre",
    ECC."DocumentoIdentidad" 														AS "SocNeg",
    TO_NVARCHAR(ECC."IdPortafolio") 												AS "Port",
    PORT."CodigoPortafolio" 														AS "CodigoPortafolio",
    PORT."Proposito"																AS "PropositoPortafolio",
    ECC."IdPortafolio",
    COALESCE(CPT."Numero", '') 														AS "Fono",
    COALESCE(CPDE."Direccion", '') 													AS "Mail",
    CEMP."NombreCompleto" 														AS "Agente",
    TO_VARCHAR(TO_DATE(:p_FechaSistema, 'YYYY-MM-DD'), 'DD-MM-YYYY') 				AS "Fecha",
    :p_Hora 																		AS "Hora",               
    TEC."Nombre"																AS "Tipo",
    ECC."Nemo" 																		AS "Familia",
    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
    CASE ECC."IdTipoEventoCapital"
        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."MontoInteres",0)) AS NVARCHAR)
        WHEN 'DA'   THEN    CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
        WHEN 'DA'   THEN    CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
        ELSE CASE WHEN EC."IdMoneda" <> ECC."IdMoneda" THEN CAST(IFNULL(ECC."FactorOtraMoneda",0) AS NVARCHAR)
        	ELSE CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
        	END 
	    END,
	    CASE WHEN ECC."IdMoneda"='CLP' THEN 'i' ELSE 'd' END,
	    CASE WHEN ECC."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "Pago",
    CASE ECC."IdTipoEventoCapital"
        WHEN 'DV' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'CC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'RC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'DA' THEN 'Unidades'
        WHEN 'DS' THEN 'Unidades'
        WHEN 'CA' THEN 'Unidades'                                                                       
        WHEN 'EP' THEN 'Unidades'
        ELSE 'Falta'                                                
    END 																			AS "PagoMo",
     "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
	    CASE ECC."IdTipoEventoCapital"
        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."MontoCapital",0)) AS NVARCHAR)
	    END,
	    CASE WHEN ECC."IdMoneda"='CLP' THEN 'i' ELSE 'd' END,
	    CASE WHEN ECC."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "MontoCapital", 
	CASE ECC."IdTipoEventoCapital"
        WHEN 'DV' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'CC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'RC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN 'DA' THEN 'Unidades'
        WHEN 'DS' THEN 'Unidades'
        WHEN 'CA' THEN 'Unidades'                                                                       
        WHEN 'EP' THEN 'Unidades'
        ELSE 'Falta'                                                
    END 																			AS "MontoCapitalMo",
    CASE ECC."IdMoneda"
		WHEN 'CLP' THEN REPLACE(
						"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
							CASE ECC."IdTipoEventoCapital"
						        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."Total",0)) AS NVARCHAR)
						        WHEN 'DA'   THEN    CAST(ROUND(ECC."CantidadNueva") AS NVARCHAR)
						        WHEN 'EP'   THEN    CAST(ROUND(ECC."CantidadNueva") AS NVARCHAR)                                    
						        ELSE                CAST(ROUND(ECC."Total") AS NVARCHAR)
						    END,'i',0)
						    	,'-.','-')
		ELSE REPLACE(
						"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
							CASE ECC."IdTipoEventoCapital"
						        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."Total",0),3) AS NVARCHAR)
						        WHEN 'DA'   THEN    CAST(ROUND(ECC."CantidadNueva",3) AS NVARCHAR)
						        WHEN 'EP'   THEN    CAST(ROUND(ECC."CantidadNueva",3) AS NVARCHAR)                                    
						        ELSE                CAST(ROUND(ECC."Total",3) AS NVARCHAR)
						    END,'d',3)
						    	,'-.','-')			    
    END 
    AS "Monto",
    --Identificar y Remover el signo menos para la conversión de formato o dar formato de miles
    CASE 
	    WHEN 
	    	MOD(LENGTH(FLOOR(ECC."CantidadDerecho")),3)>0 AND SUBSTRING(FLOOR(ECC."CantidadDerecho"),1,1)='-' 
	    	THEN 
	    	'-'||"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
	    	CAST(IFNULL(
		    	SUBSTRING(FLOOR(ECC."CantidadDerecho"),2,LENGTH(FLOOR(ECC."CantidadDerecho")))--Solo números 
		    	,0) AS NVARCHAR)
	    	,'i',0)	
	    ELSE 
	    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
	    	CAST(IFNULL(FLOOR(ECC."CantidadDerecho"),0) AS NVARCHAR)
	    	,'i',0)					
	END																				AS "Cantidad", 
	CASE 
        WHEN ECC."IdTipoEventoCapital" = 'DV' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN ECC."IdTipoEventoCapital" = 'CC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN ECC."IdTipoEventoCapital" = 'RC' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN ECC."IdTipoEventoCapital" = 'DA' AND EC."IdEstado" <> 'CER' THEN 'Unidades'
        WHEN ECC."IdTipoEventoCapital" = 'DS' AND EC."IdEstado" <> 'CER' THEN 'Unidades'
        WHEN ECC."IdTipoEventoCapital" = 'CA' AND EC."IdEstado" <> 'CER' THEN 'Unidades'                                   
        WHEN ECC."IdTipoEventoCapital" = 'EP' AND EC."IdEstado" <> 'CER' THEN 'Unidades'
        WHEN ECC."IdTipoEventoCapital" = 'SL' THEN IFNULL(ECC."IdMoneda",'No existe información')
        WHEN ECC."IdTipoEventoCapital" IN ('DA','EP','EL') AND EC."IdEstado"='CER' THEN ''
        ELSE 'Falta'                                                
    END 																			AS "MontoMo",

    ECC."IdTipoEventoCapital"                                                       AS "TipoEventoCapital",
    TO_VARCHAR(TO_DATE(ECC."FechaPago", 'YYYY-MM-DD'), 'DD-MM-YYYY') 				AS "FechaPago",
    ES."Descripcion" 																AS "Estado",
    --pie
    CCL."IdDireccionElectronicaPrincipal",
    CCL."IdPersona" 																AS "IdPersonaDestinatario",
    CCDG."RazonSocial"																AS "Corredora",
    LOWER(CCDG."Email")																AS "MailCorredora",
    CASE WHEN CPDEJ."Direccion" = '' OR CPDEJ."Direccion" IS NULL THEN LOWER(PLA."Valor")
    ELSE LOWER(CPDEJ."Direccion") END    								            AS "MailAgente",
    CASE 
			WHEN ECC."IdMoneda" <> EC."IdMoneda" AND ECC."FactorOtraMoneda" = EC."Factor" THEN COALESCE(CAST((SELECT "Factor" 
																											  FROM "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda" 
																											  WHERE "Secuencia" = EC."Secuencia" AND "IdMoneda" = ECC."IdMoneda" AND "Prioridad" = 1 and "Nemotecnico" = EC."Nemo")	AS DECIMAL(26,8)), 0)		
			WHEN ECC."IdMoneda" <> EC."IdMoneda" AND ECC."FactorOtraMoneda" <> EC."Factor" THEN COALESCE(CAST(ECC."FactorOtraMoneda" AS DECIMAL(26,8)), 0)	
			ELSE COALESCE(CAST(EC."Factor" AS DECIMAL(26,8)), 0)	
	END		  																		AS "Factor",
    CCDG."Telefono"                                                                 AS "AtencionCliente",
    CURRENT_UTCTIMESTAMP															AS "FechaCreacionSistema",
    BOV."Nombre"                                                         			AS "Boveda",
    CASE 
    	WHEN ECC."IdTipoInstrumento" = 'ACN' AND EC."IdEstado" <> 'CER' THEN 'ACCIONES' 
    	WHEN ECC."IdTipoInstrumento" = 'ACN' AND EC."IdEstado"  = 'CER' THEN 'Custodia'
    	ELSE  ECC."IdTipoInstrumento" END AS "IdTipoInstrumento",
    EC."FechaLimite",
    CASE 
    WHEN EC."IdEstado"='CER' AND ECC."IdTipoEventoCapital" = 'CC' THEN 'CIERRE_EVENTO_CAPITAL_'||ECC."IdTipoEventoCapital" 
    WHEN EC."IdEstado"='CER' AND ECC."IdTipoEventoCapital" = 'DV' THEN 'CIERRE_EVENTO_CAPITAL_'||ECC."IdTipoEventoCapital" 
    WHEN EC."IdEstado"='CER' AND ECC."IdTipoEventoCapital" = 'SL' THEN 'CIERRE_EVENTO_CAPITAL_'||ECC."IdTipoEventoCapital" --SOLO LIBERAR SI APLICA EL SORTEO DE LETRA
    WHEN EC."IdEstado"='CER' AND ECC."IdTipoEventoCapital" IN('DA','RC','EP','EL') THEN 'CIERRE_EVENTO_CAPITAL_VARIOS' 
    ELSE TEC."CodigoTipoEvento" END AS "CodigoTipoEvento",
    INS."RazonSocial"																AS "RazonSocial",
    MAP(MAT."IdProductoAgrupador",NULL,9999,MAT."IdProductoAgrupador") 				AS "IdProductoAgrupador",
    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
    		CAST(COALESCE(EC."TasaEfectiva",0) AS nvarchar)
    	,'d',4,'CL')																		AS "TasaEfectiva",															
    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
    		CAST(COALESCE(ECC."MontoRetencionISFUT",0) AS nvarchar)
    	,'d',4,'CL') 																	AS "MontoRetencionISFUT",
    ECC."MontoImpuestoExtranjero",
    ECC."MontoGastoTransferencia" AS "GastoTransferencia",
    LPAD(TO_VARCHAR(DAYOFMONTH(TO_DATE(:p_FechaSistema, 'YYYY-MM-DD'))),2,'00') || ' de ' 
    || CASE MONTH(TO_DATE(:p_FechaSistema, 'YYYY-MM-DD'))
           WHEN 1 THEN 'Enero'
           WHEN 2 THEN 'Febrero'
           WHEN 3 THEN 'Marzo'
           WHEN 4 THEN 'Abril'
           WHEN 5 THEN 'Mayo'
           WHEN 6 THEN 'Junio'
           WHEN 7 THEN 'Julio'
           WHEN 8 THEN 'Agosto'
           WHEN 9 THEN 'Septiembre'
           WHEN 10 THEN 'Octubre'
           WHEN 11 THEN 'Noviembre'
           WHEN 12 THEN 'Diciembre'
      END
	|| ' de ' || TO_VARCHAR(YEAR(TO_DATE(:p_FechaSistema, 'YYYY-MM-DD'))) AS "FechaString",
	LPAD(TO_VARCHAR(DAYOFMONTH(TO_DATE(ECC."FechaPago", 'YYYY-MM-DD'))),2,'00') || ' de ' 
    || CASE MONTH(TO_DATE(ECC."FechaPago", 'YYYY-MM-DD'))
           WHEN 1 THEN 'Enero'
           WHEN 2 THEN 'Febrero'
           WHEN 3 THEN 'Marzo'
           WHEN 4 THEN 'Abril'
           WHEN 5 THEN 'Mayo'
           WHEN 6 THEN 'Junio'
           WHEN 7 THEN 'Julio'
           WHEN 8 THEN 'Agosto'
           WHEN 9 THEN 'Septiembre'
           WHEN 10 THEN 'Octubre'
           WHEN 11 THEN 'Noviembre'
           WHEN 12 THEN 'Diciembre'
      END
	|| ' de ' || TO_VARCHAR(YEAR(TO_DATE(ECC."FechaPago", 'YYYY-MM-DD'))) AS "FechaPagoString",
	TO_VARCHAR(TO_DATE(EC."FechaLimite", 'YYYY-MM-DD'), 'DD-MM-YYYY') AS "FechaCorte",
	COALESCE(CPTEMP."Numero", '') AS "FonoAgente",
    CASE 
	    WHEN 
	    	MOD(LENGTH(FLOOR(ECC."CantidadNueva")),3)>0 AND SUBSTRING(FLOOR(ECC."CantidadNueva"),1,1)='-' 
	    	THEN 
	    	'-'||"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
	    	CAST(IFNULL(
		    	SUBSTRING(FLOOR(ECC."CantidadNueva"),2,LENGTH(FLOOR(ECC."CantidadNueva")))--Solo números 
		    	,0) AS NVARCHAR)
	    	,'i',0)	
	    ELSE 
	    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
	    	CAST(IFNULL(FLOOR(ECC."CantidadNueva"),0) AS NVARCHAR)
	    	,'i',0)					
	END AS "CantidadNueva"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC 
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital" = ECC."IdEventoCapital"
INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEC ON ECC."IdTipoEventoCapital" = TEC."IdTipoEventoCapital"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON (ECC."IdPortafolio" = PORT."IdPortafolio")
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCL."IdCliente" = PORT."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON BOV."IdBoveda" = EC."IdBoveda"
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CCL."IdTelefonoPrincipal" = CPT."IdTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CEMP."IdEmpleado" = PORT."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDEJ ON CPDEJ."IdDireccionElectronica" = CEMP."IdDireccionElectronica"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPTEMP ON CPTEMP."IdTelefono" = CEMP."IdTelefonoPrincipal"
LEFT JOIN "BSCL.Custodia.BD::CUS.EstadoCustodia" ES ON ECC."IdEstado" = ES."IdEstado" AND ES."NombreEntidad" = 'CUSTODIA.EVENTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" PLA ON PLA."CodigoParametro" = 'EVT.MailDefectoContactoCorredora'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (INS."Nemotecnico" = EC."Nemo" AND INS."IdTipoInstrumento" = EC."IdTipoInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EM ON EM."IdPersona"=CCL."IdPersona" AND EM."IdEmisor" =INS."IdEmisor"
WHERE CPDE."IdEstado"='ACT' AND EC."IdEventoCapital" IN (:p_IdEventoCapital) 
	AND ECC."CantidadDerecho" > 0
	AND EM."IdPersona" IS NULL
ORDER BY EC."IdEventoCapital"