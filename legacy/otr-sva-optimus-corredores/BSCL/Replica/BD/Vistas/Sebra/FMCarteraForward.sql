SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMCarteraForward"; 


CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMCarteraForward" AS

SELECT * FROM (
        Select 
			RANK() OVER (PARTITION BY CAR."FolioCartera" ORDER BY CAR."IdCartera" DESC)  as RANK,
            COALESCE (CAST (CAR."FolioCartera" AS NUMERIC) , 0 )  AS "FOLIOCARTERA", -- NUMERIC
            COALESCE (CAST (OPE."IdOperacion" AS INT) , 0 )  AS "FOLIOTRANSACCION", -- INT
            --COALESCE (CAR."FolioCartera" , 0 )  AS "FOLIOOPERACION", -- INT 
            
            CASE WHEN LEFT(COALESCE (OPE."FechaCreacion" , '1900-01-01' ),10) < '2022-01-17' THEN COALESCE(OPE."FolioOperacion",CAR."FolioCartera")
			ELSE COALESCE (CAR."FolioCartera" , 0 ) END AS "FOLIOOPERACION", -- INT
            
            
            CAST(CASE
				WHEN EMP."Nombre" LIKE '%MBI%' THEN 'MBI'
				WHEN EMP."Nombre" LIKE '%MERRILL%' THEN 'MERRILL'
				WHEN EMP."Nombre" LIKE '%SURA%' THEN 'SURA'
				WHEN EMP."Nombre" LIKE '%BANCOESTADO%' THEN 'BANESTADO'
				ELSE ''
			END AS VARCHAR(10)) AS "CODFONDO", -- CHAR(10) 
			
            COALESCE ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(DOC."DocumentoIdentidad" AS VARCHAR(11))), '')  AS "RUTCLIENTE", -- CHAR(11)
            COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '-1') AS "CUENTACLIENTE", -- VARCHAR(100)
            0  AS "CODLIBRO", -- INT 
            0  AS "CODCARTERA", -- INT 
            0  AS "CODSUBCARTERA", -- INT 
            0  AS "CODCARTFINANCIERA", -- INT 
            0  AS "CODDEPTO", -- INT 
            
            2  AS "CODPRODUCTO", -- INT 
            1  AS "CODSUBPRODUCTO", -- INT 
            
            COALESCE (CASE WHEN CAR."IdConcepto" = 'VENFWD' THEN 2 ELSE 1 END, 0)  AS "CODTIPOMOVTO", -- INT
                        
            0  AS "CODENTIDAD", -- INT

            0  AS "CODSUCURSAL", -- INT
            COALESCE (HOM."ValorOrigen" , 0 )  AS "CODMONPRINC", -- INT
            COALESCE (HOMS."ValorOrigen" , 0 )  AS "CODMONSECU", -- INT
            COALESCE (CAR."INGMontoMonedaPrincipal" , 0 )  AS "MTOMONPRINC", -- DECIMAL
            COALESCE (OPE."ParidadCierre" , 0 )  AS "PARIDADCIERREMONPRINC", -- DECIMAL
            0   AS "PARIDADCIERREMONSECU", -- DECIMAL
            0   AS "TASACIERREFORWARD", -- DECIMAL
            COALESCE (OPE."TCCierre", 0 )  AS "TCCIERREFORWARD", -- DECIMAL     
            CAST(COALESCE (CAR."MontoMonedaSecundaria" , 0 ) AS DECIMAL(18,4))  AS "MTOMONSECU", -- DECIMAL
            
            0   AS "PARIDADTRANSFMONPRINC", -- DECIMA
            0   AS "PARIDADTRANSFMONSECU", -- DECIMAL
            0   AS "TASATRANSFFORWARD", -- DECIMAL
            
            COALESCE (CAR."ValorTipoCambioCosto" , 0 )  AS "TCTRANSFFORWARD", -- DECIMAL
            
            COALESCE (CAR."ValorParidadCierre" , 0 )  AS "PARIDADHOYMONPRINC", -- DECIMAL
            0   AS "PARIDADHOYMONSECU", -- DECIMAL
            COALESCE (OPE."TCCosto" , 0 )  AS "TCHOY", -- DECIMAL
            
            994   AS "CODTCREFERENCIA", -- INT
            COALESCE (OPE."TCCosto" , 0 )  AS "TCREFERENCIA", -- DECIMAL
            0   AS "TASAMONPRINC", -- DECIMAL
            0   AS "TASAMONSECU", -- DECIMAL
            0   AS "PRECIOSPOT", -- DECIMAL
            COALESCE (OPE."PuntosForward" , 0 )  AS "PUNTOFORWARD", -- DECIMAL
            COALESCE (CAR."INGTasaPuntosMonedaPrincipal" , 0 )  AS "COTTASAMONPRINC", -- DECIMAL	*		
            COALESCE (CAR."INGTasaPuntosMonedaSecundaria" , 0 )  AS "COTTASAMONSECU", -- DECIMAL	*
            COALESCE (OPE."TCCosto" , 0 )  AS "COTPRECIOSPOT", -- DECIMAL
            0   AS "COTPUNTOFORWARD", -- DECIMAL
            COALESCE (OPE."TCCosto" , 0 )  AS "COTPRECIOFORWARD", -- DECIMAL	
            0 AS "SPREADTASA", -- DECIMAL
            COALESCE (CASE WHEN PAR."ValorParametro" = 'COMPENSADA' THEN 2
            			   WHEN PAR."ValorParametro" = 'ENTREGA_FISICA' THEN 1
            			   ELSE 0 END , 0 )  AS "MODCUMPLIMIENTO", -- INT
                        
            TO_VARCHAR (CASE WHEN  COALESCE (CAR."FechaOperacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE CAR."FechaOperacion" END ,'YYYY-MM-DD HH24:MI:SS.FF3')   AS "FECHAINICIO", -- DATETIME
            
            COALESCE (PLA."Codigo" , 0 )  AS "CODPLAZA", -- INT
            COALESCE (OPE."PlazoEnDias", 0 )  AS "PLAZO", -- INT
            TO_VARCHAR (CASE WHEN  COALESCE (CAR."FechaVencimientoOperacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE CAR."FechaVencimientoOperacion" END ,'YYYY-MM-DD')   AS "FECHAVENCIMIENTO", -- DATETIME
            
            0   AS "CODCURVAMONPRINC", -- INT				xxx
            0   AS "CODCURVAMONSECU", -- INT				xxx
            COALESCE (CAR."INGMontoATasaCompraActual" , 0 )  AS "MTOTRANSFERENCIA", -- DECIMAL		
            COALESCE (CAST(CASE WHEN CAR."IdEstado" = 'VIG' THEN 'V'
            					WHEN CAR."IdEstado" = 'LIQ' THEN 'L'
            					WHEN CAR."IdEstado" = 'VCD' THEN 'L'
            					WHEN CAR."IdEstado" = 'MOD' THEN 'M'
            					WHEN CAR."IdEstado" = 'ANU' THEN 'A'
            					ELSE '' END AS VARCHAR(1)),'')  AS "ESTADO", -- CHAR(1)		-- A / L / M / V
			COALESCE (CAST(CASE WHEN CAR."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOLIMITE", -- CHAR(1)				
            COALESCE (CAST(CASE WHEN CAR."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOCONFIRMADOR", -- CHAR(1)
            COALESCE (CAST(CASE WHEN CAR."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOCTLFINANC", -- CHAR(1)
            COALESCE (CAR."IdFixing" , 0 )  AS "CODFIXING", -- INT
            TO_VARCHAR (CASE WHEN  COALESCE (CAR."FechaFixing", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE CAR."FechaFixing" END ,'YYYY-MM-DD HH24:MI:SS.FF3')   AS "FECHAFIXING", -- DATETIME
            
            
            ''   AS "INDNDF", -- CHAR(1)
            ''   AS "INDPERMITEANTICIPO", -- CHAR(1)
            ''   AS "MARCA1", -- VARCHAR(50)
            ''   AS "MARCA2", -- VARCHAR(50)
            ''   AS "MARCA3", -- VARCHAR(50)
            ''   AS "MARCA4", -- VARCHAR(50)
            ''   AS "MARCA5", -- VARCHAR(50)
            '1'  AS "TIPOPAGO", -- CHAR(2)	-- 1
            COALESCE (CAST (usu."CodigoUsuario" AS VARCHAR(15)), '')  AS "USUARIOCREADOR", -- VARCHAR(15)
            LEFT(TO_VARCHAR (CASE WHEN  COALESCE (OPE."FechaCreacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE OPE."FechaCreacion" END ,'YYYY-MM-DD HH24:MI:ss.FF3'),10)   AS "FECHACREACION", -- DATETIME
            
            ''  AS "TERMINAL", -- CHAR(16)
            0   AS "MTOUTILIDADDIFERIDA", -- DECIMAL
            0   AS "MTOPERDIDADIFERIDA", -- DECIMAL
            COALESCE (CAR."INGMontoValorRazonableActivo" , 0 )  AS "MTOVRACTIVO", -- DECIMAL						
            COALESCE (CAR."INGMontoValorRazonablePasivo" , 0 )  AS "MTOVRPASIVO", -- DECIMAL
            COALESCE (CAR."INGMontoValorRazonableNeto" , 0 )  AS "MTOVRNETO", -- DECIMAL
            COALESCE (CAR."INGMontoDiferido", 0 )  AS "MTODIFERIDO", -- DECIMAL				
            COALESCE (CAR."INGTasaValorRazonableActivo", 0 )  AS "TASAVRACTIVO", -- DECIMAL
            COALESCE (CAR."INGTasaValorRazonablePasivo", 0 )  AS "TASAVRPASIVO", -- DECIMAL
            COALESCE (CAR."INGMontoMonedaSecundaria" , 0 )  AS "MTOMONSECUINI", -- DECIMAL

            COALESCE (CASE WHEN PAR."ValorParametro" = 'COMPENSADA' THEN CAR."EGRMontoLiquidacion"
            			   ELSE 0 END , 0 )  AS "MTOCOMPENSACION", -- DECIMAL            
           
                TO_VARCHAR(
                    CASE
                        WHEN OPE."IdEstado" = 'VIG' THEN COALESCE(CAR."FechaVencimientoOperacion", '1900-01-01')
                        ELSE COALESCE(CASE OPE."FechaLiquidacion" WHEN '0001-01-01 00:00:00.000' THEN '1900-01-01' ELSE OPE."FechaLiquidacion" END, '1900-01-01')
                    END
                , 'YYYY-MM-DD') AS "FECHAVCTOANT", -- DATETIME    
                
            COALESCE (CAR."EGRMontoLiquidacion" , 0 )  AS "MTOLIQUIDACION", -- DECIMAL
            0   AS "TASAANTICIPO", -- DECIMAL
            0   AS "PRECIOLIQUIDACION", -- DECIMAL
            0   AS "FACTORRIESGO", -- DECIMAL
            0   AS "MTOUTILIZADORIESGO", -- DECIMAL
            0   AS "MTORIESGOTOTAL", -- DECIMAL
            COALESCE (CAR."INGMontoMargen" , 0 )  AS "MARGEN", -- DECIMAL
            0   AS "MTOCALCE", -- DECIMAL
            0   AS "MTOCALCEDISP", -- DECIMAL								
            COALESCE (CAST('' AS VARCHAR(100)),'')  AS "OBSERVACIONESCALCE", -- VARCHAR(100)
            COALESCE (CAST('' AS VARCHAR(15)),'')  AS "USURESERVA", -- VARCHAR(15)
            0   AS "CODSUBLIBRO", -- INT
            0   AS "CODLIBRO1", -- INT
            0   AS "CODSUBLIBRO1", -- INT
            0   AS "PARIDADVCTO", -- DECIMAL
            0   AS "MTODEVTC", -- DECIMAL
            0   AS "MTODEVTCANT", -- DECIMAL
            0   AS "MTODIFDIARIO", -- DECIMAL
            0   AS "MTODIFACUM", -- DECIMAL
            0   AS "CODSUBSUBLIBRO", -- INT
            
            TO_VARCHAR (CASE WHEN  COALESCE (OPE."FechaLiquidacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE OPE."FechaLiquidacion" END ,'YYYY-MM-DD HH24:MI:SS.FF3')   AS "FECEFECTIVA", -- DATETIME
            
            0   AS "ANTPLAZO", -- INT
            0   AS "ANTPARMP", -- DECIMAL
            0   AS "ANTPARCLP", -- DECIMAL
            0   AS "ANTPARMPCOS", -- DECIMAL
            0   AS "ANTPARMSCOS", -- DECIMAL
            0   AS "ANTPTOPARMP", -- DECIMAL
            0   AS "ANTPTOPARMS", -- DECIMAL
            0   AS "ANTPTOPARMPCOS", -- DECIMAL
            0   AS "ANTPTOPARMSCOS", -- DECIMAL
            0   AS "ANTTASAMS", -- DECIMAL
            0   AS "ANTPARMPENTFIS", -- DECIMAL
            0   AS "ANTPARMSENTFIS", -- DECIMAL
            0   AS "ANTPARMS", -- DECIMAL
            0   AS "ANTCODMONCMP", -- INT
            0   AS "ANTPARMONCMP", -- DECIMAL
            0   AS "ANTPOT10AJUSTE", -- INT
            0   AS "ANTEFEVALPTEUNIMS", -- DECIMAL
            0   AS "ANTMTMSPOTMS", -- DECIMAL
            0   AS "ANTMTMMS", -- DECIMAL
            0   AS "ANTMTMCLP", -- DECIMAL
            0   AS "ANTMTMLIQUIDARMS", -- DECIMAL
            0   AS "ANTMTMCOSTOMS", -- DECIMAL
            0   AS "ANTMTMCOSTOCLP", -- DECIMAL
            0   AS "ANTMTMAJUSTEMONCMP", -- DECIMAL
            0   AS "ANTMTMAJUSTECLP", -- DECIMAL
            0   AS "ANTMARGENCLP", -- DECIMAL
            0   AS "ANTMTOMONSECU", -- DECIMAL
            0   AS "MOVTOLIQUIDACION", -- DECIMAL
            0   AS "MOVTOCAJA", -- DECIMAL
			OPE."IdOperacion",
			CAR."FolioCartera",
			CASE
				WHEN CAR."IdEstado" = 'LIQ' AND CAR."EGRIdMonedaLiquidacion" != '' THEN CAR."EGRIdMonedaLiquidacion"
				ELSE 'CLP' END													AS "IDMONEDALIQUIDACION"
        FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
        INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE ON OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto"
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CAR."IdCliente" = CLI."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu on usu."IdUsuario" = CAR."IdUsuario"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON CAR."IdEmpresa" = EMP."IdEmpresa"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND POR."IdPortafolio" = car."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON ON MON."IdMoneda" = CAR."IdMonedaPrincipal"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONS ON MONS."IdMoneda" = CAR."IdMonedaSecundaria"
		LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" PLA ON CAR."IdPlaza" = PLA."IdPlaza"
		LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PAR ON OPE."IdModalidadLiquidacion" = PAR."IdParametro" AND PAR."NombreTipoParametro" = 'ModalidadLiquidacion'
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" HOM ON HOM."TipoValor" = 'MonedaDivisas' AND  MON."Codigo" = HOM."ValorDestino"
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" HOMS ON HOMS."TipoValor" = 'MonedaDivisas' AND  MONS."Codigo" = HOMS."ValorDestino"
        WHERE CAR."IdModulo" = 'MFW' --and CAR."IdEstado" = 'VIG'
) WHERE RANK = 1

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMCarteraForward" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
