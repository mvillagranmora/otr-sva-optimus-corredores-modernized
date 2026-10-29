SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFMCarteraForward"; 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFMCarteraForward" AS

        Select WST."IdStack" AS "IDSTACK","Action" AS ACTION,
        	cast("FOLIOCARTERA" AS varchar) AS TABLE_KEY,
            "FOLIOCARTERA", -- NUMERIC
            "FOLIOTRANSACCION", -- INT
            "FOLIOOPERACION", -- INT 
            "CODFONDO", -- CHAR(10) 
            "RUTCLIENTE", -- CHAR(11)
            "CUENTACLIENTE", -- VARCHAR(100)
            "CODLIBRO", -- INT
            "CODCARTERA", -- INT 
            "CODSUBCARTERA", -- INT 
            "CODCARTFINANCIERA", -- INT 
            "CODDEPTO", -- INT 
            
            "CODPRODUCTO", -- INT 
            "CODSUBPRODUCTO", -- INT 
            
            "CODTIPOMOVTO", -- INT
                        
            "CODENTIDAD", -- INT

            "CODSUCURSAL", -- INT
            "CODMONPRINC", -- INT
            "CODMONSECU", -- INT
            "MTOMONPRINC", -- DECIMAL
            "PARIDADCIERREMONPRINC", -- DECIMAL
            "PARIDADCIERREMONSECU", -- DECIMAL
            "TASACIERREFORWARD", -- DECIMAL
            "TCCIERREFORWARD", -- DECIMAL     
            "MTOMONSECU", -- DECIMAL
            
            "PARIDADTRANSFMONPRINC", -- DECIMA
            "PARIDADTRANSFMONSECU", -- DECIMAL
            "TASATRANSFFORWARD", -- DECIMAL
            
            "TCTRANSFFORWARD", -- DECIMAL
            
            "PARIDADHOYMONPRINC", -- DECIMAL
            "PARIDADHOYMONSECU", -- DECIMAL
            "TCHOY", -- DECIMAL
  
            "CODTCREFERENCIA", -- INT
            "TCREFERENCIA", -- DECIMAL
            "TASAMONPRINC", -- DECIMAL
            "TASAMONSECU", -- DECIMAL
            "PRECIOSPOT", -- DECIMAL
            "PUNTOFORWARD", -- DECIMAL
            "COTTASAMONPRINC", -- DECIMAL	*		
            "COTTASAMONSECU", -- DECIMAL	*
            "COTPRECIOSPOT", -- DECIMAL
            "COTPUNTOFORWARD", -- DECIMAL
            "COTPRECIOFORWARD", -- DECIMAL	
            "SPREADTASA", -- DECIMAL
            "MODCUMPLIMIENTO", -- INT      
            "FECHAINICIO", -- DATETIME
            "CODPLAZA", -- INT
            "PLAZO", -- INT
            "FECHAVENCIMIENTO", -- DATETIME
            "CODCURVAMONPRINC", -- INT				xxx
            "CODCURVAMONSECU", -- INT				xxx
            "MTOTRANSFERENCIA", -- DECIMAL		
            "ESTADO", -- CHAR(1)		-- A / L / M / V
            "ESTADOLIMITE", -- CHAR(1)
            "ESTADOCONFIRMADOR", -- CHAR(1)
            "ESTADOCTLFINANC", -- CHAR(1)
            "CODFIXING", -- INT
            "FECHAFIXING", -- DATETIME
            "INDNDF", -- CHAR(1)
            "INDPERMITEANTICIPO", -- CHAR(1)
            "MARCA1", -- VARCHAR(50)
            "MARCA2", -- VARCHAR(50)
            "MARCA3", -- VARCHAR(50)
            "MARCA4", -- VARCHAR(50)
            "MARCA5", -- VARCHAR(50)
            "TIPOPAGO", -- CHAR(2)	-- 1
            "USUARIOCREADOR", -- VARCHAR(15)
            "FECHACREACION", -- DATETIME
            "TERMINAL", -- CHAR(16)
            "MTOUTILIDADDIFERIDA", -- DECIMAL
            "MTOPERDIDADIFERIDA", -- DECIMAL
            "MTOVRACTIVO", -- DECIMAL						
            "MTOVRPASIVO", -- DECIMAL
            "MTOVRNETO", -- DECIMAL
            "MTODIFERIDO", -- DECIMAL				
            "TASAVRACTIVO", -- DECIMAL
            "TASAVRPASIVO", -- DECIMAL
            "MTOMONSECUINI", -- DECIMAL
            "MTOCOMPENSACION", -- DECIMAL
            "FECHAVCTOANT", -- DATETIME
            "MTOLIQUIDACION", -- DECIMAL
            "TASAANTICIPO", -- DECIMAL
            "PRECIOLIQUIDACION", -- DECIMAL
            "FACTORRIESGO", -- DECIMAL
            "MTOUTILIZADORIESGO", -- DECIMAL
            "MTORIESGOTOTAL", -- DECIMAL
            "MARGEN", -- DECIMAL
            "MTOCALCE", -- DECIMAL
            "MTOCALCEDISP", -- DECIMAL								
            "OBSERVACIONESCALCE", -- VARCHAR(100)
            "USURESERVA", -- VARCHAR(15)
            "CODSUBLIBRO", -- INT
            "CODLIBRO1", -- INT
            "CODSUBLIBRO1", -- INT
            "PARIDADVCTO", -- DECIMAL
            "MTODEVTC", -- DECIMAL
            "MTODEVTCANT", -- DECIMAL
            "MTODIFDIARIO", -- DECIMAL
            "MTODIFACUM", -- DECIMAL
            "CODSUBSUBLIBRO", -- INT
            "FECEFECTIVA", -- DATETIME
            "ANTPLAZO", -- INT
			"ANTPARMP", -- DECIMAL
			"ANTPARCLP", -- DECIMAL
			"ANTPARMPCOS", -- DECIMAL
			"ANTPARMSCOS", -- DECIMAL
			"ANTPTOPARMP", -- DECIMAL
			"ANTPTOPARMS", -- DECIMAL
			"ANTPTOPARMPCOS", -- DECIMAL
			"ANTPTOPARMSCOS", -- DECIMAL
			"ANTTASAMS", -- DECIMAL
			"ANTPARMPENTFIS", -- DECIMAL
			"ANTPARMSENTFIS", -- DECIMAL
			"ANTPARMS", -- DECIMAL
			"ANTCODMONCMP", -- INT
			"ANTPARMONCMP", -- DECIMAL
			"ANTPOT10AJUSTE", -- INT
			"ANTEFEVALPTEUNIMS", -- DECIMAL
			"ANTMTMSPOTMS", -- DECIMAL
			"ANTMTMMS", -- DECIMAL
			"ANTMTMCLP", -- DECIMAL
			"ANTMTMLIQUIDARMS", -- DECIMAL
			"ANTMTMCOSTOMS", -- DECIMAL
			"ANTMTMCOSTOCLP", -- DECIMAL
			"ANTMTMAJUSTEMONCMP", -- DECIMAL
			"ANTMTMAJUSTECLP", -- DECIMAL
			"ANTMARGENCLP", -- DECIMAL
			"ANTMTOMONSECU", -- DECIMAL
			"MOVTOLIQUIDACION", -- DECIMAL
			"MOVTOCAJA" -- DECIMAL

        FROM "BSCL.Replica.BD.Vistas::Sebra.FMCarteraForward" FWD
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FMCarteraForward' AND WST."Key" = cast(FWD."FOLIOCARTERA"  AS varchar) 


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFMCarteraForward" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
