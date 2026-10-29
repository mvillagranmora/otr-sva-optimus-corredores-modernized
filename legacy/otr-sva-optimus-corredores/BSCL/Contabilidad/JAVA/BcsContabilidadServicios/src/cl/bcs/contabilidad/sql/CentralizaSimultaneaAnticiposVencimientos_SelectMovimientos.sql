SELECT
    TO_BIGINT(:p_IdTipoCentralizacion)                    AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"                        AS "IdMovimiento",
    CASE ConsultarCartera."EGRIdConcepto"
        WHEN 'VCTSIC'   THEN 'VENSIMCOMP'           
        WHEN 'VCTSIV'   THEN 'VENSIMVENT'
        WHEN 'ANTSIC'   THEN 'ANTSIMCOMP'           
        WHEN 'ANTSIV'   THEN 'ANTSIMVENT'
        ELSE CASE "EGRIdTipoOperacion" 
        		WHEN 'VENSIM' THEN 'VENSIMVENT' 
        		WHEN 'COMSIM' THEN 'VENSIMCOMP'
        		ELSE '????????????' 
    		 END  
    END                                                 AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    ConsultarCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."INGFechaOperacion")  AS "Periodo",
    CASE 
    	WHEN ConsultarCartera."EGRIdConcepto" IN ('ANTSIC', 'ANTSIV') THEN ROUND(ABS(ConsultarCartera."EGRMontoARecibirAnticipo"),0)
    	WHEN ConsultarCartera."EGRIdConcepto" IN ('VCTSIC', 'VCTSIV', 'VNSIMU') THEN ROUND(ABS(ConsultarCartera."INGMontoAPlazo") ,0)
    	ELSE CASE  
        	WHEN "EGRIdTipoOperacion" IN ('VENSIM', 'COMSIM') THEN ROUND(ABS(ConsultarCartera."EGRMonto") ,0)
    	END 
    END AS "Monto",
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",                                  
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa"          = :p_IdEmpresa 
    AND "EGRFechaOperacion"                 = :p_FechaContable
    AND ("EGRIdConcepto" IN ('VCTSIC','VCTSIV','ANTSIC','ANTSIV') OR "EGRIdTipoOperacion" IN ('VENSIM','COMSIM'))
    AND "IdClasificacionTipoInstrumento"    = 'RV' 
    AND "IdTipoInstrumento"                 = 'SIM'
    AND ConsultarCartera."IdEstado" != 'ANU';
  
