SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMMovtosForward"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMMovtosForward" AS

	WITH 
		Cartera as (
			SELECT 
					RANK() OVER (PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC)  as RANK,
					--"IdCartera", 
					"IdEmpresa", "FolioCartera", "IdCliente", "IdPortafolio", "FolioComprobante", "FechaComprobante", "IdOperacion", "FechaOperacion",
				   "IdTipoProducto", "IdMonedaPrincipal", "IdMonedaSecundaria", "MontoMonedaPrincipal" AS "MontoMonedaPrincipalIngreso",
				   "MontoMonedaSecundaria" AS "MontoMonedaSecundariaIngreso",
				   CASE WHEN "EGRIdTipoOperacion" = 'ANTFWD' THEN "EGRMontoMonedaPrincipal" 
						ELSE "INGMontoMonedaPrincipal" 
				   END AS "MontoMonedaPrincipalING",
				   CASE WHEN "EGRIdTipoOperacion" = 'ANTFWD' THEN "EGRMontoMonedaSecundaria" 
						ELSE "INGMontoMonedaSecundaria" 
				   END AS "MontoMonedaSecundariaING", 
				   
				   "IdFixing","FechaFixing","INGMontoMargen" AS "MontoMargen","ValorParidadCierre",
				   "IdSucursal","IdUsuario","IdAgente","IdPlaza","Glosa","FechaVencimientoOperacion",
				   
				   "ValorTipoCambioCierre","ValorTipoCambioCosto","ValorParidadCosto",
				   "INGMontoDiferido" AS "MontoDiferido","INGMontoDiferido" AS "MontoDiferido", "IdTipoOperacion",
				   "EGRMontoLiquidacion", "IdEstado","EGRIdMonedaLiquidacion"
			FROM "BSCL.Custodia.BD::CUS.Cartera" 
			WHERE "IdTipoInstrumento" = 'FWD'
		)
        Select  
            OPE."IdOperacion",
            COALESCE (OPE."IdOperacion"  , 0 )  AS "FOLIOTRANSACCION", -- INT
            --COALESCE (OPE."FolioOperacion"  , 0 )  AS "FOLIOOPERACION", -- INT
			--COALESCE (SINFO."FolioCartera" , 0 )  AS "FOLIOOPERACION", -- INT
            
            
			CASE WHEN LEFT(COALESCE (OPE."FechaCreacion" , '1900-01-01' ),10) < '2022-01-17' THEN COALESCE(OPE."FolioOperacion",SINFO."FolioCartera")
			ELSE COALESCE (SINFO."FolioCartera" , 0 ) END AS "FOLIOOPERACION", -- INT
            
            
			TO_VARCHAR (CASE WHEN  COALESCE (SINFO."FechaOperacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE SINFO."FechaOperacion" END ,'YYYY-MM-DD HH24:MI:SS.FF3')  AS "FECHAOPERACION", -- DATETIME
            
            COALESCE (SINFO."FolioCartera" , 0 )  AS "FOLIOCARTERA", -- NUMERIC
            CAST(CASE
				WHEN EMP."Nombre" LIKE '%MBI%' THEN 'MBI'
				WHEN EMP."Nombre" LIKE '%MERRILL%' THEN 'MERRILL'
				WHEN EMP."Nombre" LIKE '%SURA%' THEN 'SURA'
				WHEN EMP."Nombre" LIKE '%BANCOESTADO%' THEN 'BANESTADO'
				ELSE ''
			END AS VARCHAR(10)) AS "CODFONDO", -- CHAR(10) 
            LEFT(COALESCE ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( PER."DocumentoIdentidad" AS VARCHAR(11))), '0-0'),11)  AS "RUTCLIENTE", -- CHAR(11)
            COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)),'-1')  AS "CUENTACLIENTE", -- VARCHAR(100)
            COALESCE ( 0 , 0 )  AS "CODLIBRO", -- INT
            COALESCE ( 0 , 0 )  AS "CODCARTERA", -- INT
            COALESCE ( 0 , 0 )  AS "CODSUBCARTERA", -- INT
            COALESCE ( 0 , 0 )  AS "CODCARTFINANCIERA", -- INT
            COALESCE ( 0 , 0 )  AS "CODDEPTO", -- INT
            
            2 AS "CODPRODUCTO", -- INT
            1 AS "CODSUBPRODUCTO", -- INT
            
            CASE WHEN SINFO."IdTipoOperacion" = 'VENFWD' THEN 2 ELSE 1 END AS "CODTIPOMOVTO", -- INT
            1  AS "CODENTIDAD", -- INT
            COALESCE (SINFO."IdSucursal" , 0 )  AS "CODSUCURSAL", -- INT
            COALESCE (MON."CodigoNumerico" , 0 )  AS "CODMONPRINC", -- INT
            COALESCE (MONS."CodigoNumerico" , 0 )  AS "CODMONSECU", -- INT
            COALESCE (SINFO."MontoMonedaPrincipalING" , 0 )  AS "MTOMONPRINC", -- DECIMAL
            COALESCE (SINFO."ValorParidadCierre" , 0 )  AS "PARIDADCIERREMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADCIERREMONSECU", -- DECIMAL
            CAST(COALESCE (OPE."TCForward" , 0 ) AS DECIMAL(20,10))  AS "TASACIERREFORWARD", -- DECIMAL
            COALESCE (OPE."TCCierre" , 0 )  AS "TCCIERREFORWARD", -- DECIMAL
            CAST(COALESCE (SINFO."MontoMonedaSecundariaING" , 0 ) AS DECIMAL(18,4))  AS "MTOMONSECU", -- DECIMAL
            --COALESCE (OPE."MontoOperacionFinal", 0 )  AS "MTOMONSECU", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADTRANSFMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADTRANSFMONSECU", -- DECIMAL
            COALESCE (OPE."TCCierre" , 0 )  AS "TASATRANSFFORWARD", -- DECIMAL
            COALESCE (OPE."TCForward" , 0 )  AS "TCTRANSFFORWARD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADHOYMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PARIDADHOYMONSECU", -- DECIMAL
            COALESCE (OPE."TCCierre" , 0 )  AS "TCHOY", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CODTCREFERENCIA", -- INT
            COALESCE ( 0 , 0 )  AS "TCREFERENCIA", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "TASAMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "TASAMONSECU", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PRECIOSPOT", -- DECIMAL
            COALESCE (OPE."PuntosForward" , 0 )  AS "PUNTOFORWARD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COTTASAMONPRINC", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COTTASAMONSECU", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COTPRECIOSPOT", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COTPUNTOFORWARD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COTPRECIOFORWARD", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "SPREADTASA", -- DECIMAL
            COALESCE (CASE WHEN PAR."ValorParametro" = 'COMPENSADA' THEN 2
            			   WHEN PAR."ValorParametro" = 'ENTREGA_FISICA' THEN 1
            			   ELSE 0 END , 0 )  AS "MODCUMPLIMIENTO", -- INT --
            
            TO_VARCHAR (CASE WHEN  COALESCE (SINFO."FechaOperacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE SINFO."FechaOperacion" END ,'YYYY-MM-DD HH24:MI:SS.FF3')  AS "FECHAINICIO", -- DATETIME
            
            COALESCE (SINFO."IdPlaza" , 0 )  AS "CODPLAZA", -- INT
            COALESCE (OPE."PlazoEnDias" , 0 )  AS "PLAZO", -- INT
            
            TO_VARCHAR (CASE WHEN  COALESCE (SINFO."FechaVencimientoOperacion", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE SINFO."FechaVencimientoOperacion" END ,'YYYY-MM-DD')  AS "FECHAVENCIMIENTO", -- DATETIME
            
            
            COALESCE ( 0 , 0 )  AS "CODCURVAMONPRINC", -- INT
            COALESCE ( 0 , 0 )  AS "CODCURVAMONSECU", -- INT
            COALESCE (SINFO."MontoMonedaPrincipalING" , 0 )  AS "MTOTRANSFERENCIA", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MTOCOMISION", -- DECIMAL
            COALESCE (OPE."MontoMargen" , 0 )  AS "MTORENTABILIDAD", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "INDVIENENVAMOS", -- CHAR(1) -- A V ??
            COALESCE (CAST(SINFO."Glosa" AS VARCHAR(100)),'')  AS "OBSERVACIONES", -- VARCHAR(100)
            COALESCE (CAST(OPE."IdTipoCanalIngreso" AS VARCHAR(20)),'')  AS "ORIGEN", -- VARCHAR(20)
            COALESCE (CAST('' AS VARCHAR(30)),'')  AS "FOLIOINTERFAZ", -- VARCHAR(30)
            
            COALESCE( CAST((AG."Nombres" ||' '||AG."ApellidoPaterno") AS VARCHAR(15)),'')  AS "AGENTECOMERCIAL", -- VARCHAR(15)
            
            COALESCE (CAST(CASE WHEN SINFO."IdEstado" = 'VIG' THEN 'V'
            					WHEN SINFO."IdEstado" = 'LIQ' THEN 'L'
            					WHEN SINFO."IdEstado" = 'VCD' THEN 'L'
            					WHEN SINFO."IdEstado" = 'ANT' THEN 'L'
            					WHEN SINFO."IdEstado" = 'MOD' THEN 'M'
            					WHEN SINFO."IdEstado" = 'ANU' THEN 'A'
            					ELSE '' END AS VARCHAR(1)),'')  AS "ESTADO", -- CHAR(1)
            					
            COALESCE (CAST(CASE WHEN SINFO."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOLIMITE", -- CHAR(1)				
            COALESCE (CAST(CASE WHEN SINFO."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOCONFIRMADOR", -- CHAR(1)
            COALESCE (CAST(CASE WHEN SINFO."IdEstado" <> 'ANU' AND OPE."IdEstado" <> 'PEN' THEN 'C' ELSE '' END AS VARCHAR(1)),'')  AS "ESTADOCTLFINANC", -- CHAR(1)
	            /*--CUANDO HABILITEN CONFIRMACION DE FORWARD
				WHEN OPE."EstadoConfirmacion" = 'CNF' THEN 'C' --CONFIRMADO
				WHEN OPE."EstadoConfirmacion" = 'CEP' THEN 'P' --CONFIRMADA EN PLAZO
				WHEN OPE."EstadoConfirmacion" = 'CFP' THEN 'P' --CONFIRMADA FUERA DE PLAZO
				WHEN OPE."EstadoConfirmacion" = 'NCF' THEN '' --NO CONFIRMADA
				WHEN OPE."EstadoConfirmacion" = 'PEN' THEN '' --PENDIENTE
				*/
            COALESCE (OPE."IdFixing" , 0 )  AS "CODFIXING", -- INT
            
            TO_VARCHAR (CASE WHEN  COALESCE (OPE."FechaFixing", '1900-01-01' ) < '1900-01-01' THEN '1900-01-01' ELSE OPE."FechaFixing" END ,'YYYY-MM-DD')  AS "FECHAFIXING", -- DATETIME
            
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "INDNDF", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "INDPERMITEANTICIPO", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(50)),'')  AS "MARCA1", -- VARCHAR(50)
            COALESCE (CAST('' AS VARCHAR(50)),'')  AS "MARCA2", -- VARCHAR(50)
            COALESCE (CAST('' AS VARCHAR(50)),'')  AS "MARCA3", -- VARCHAR(50)
            COALESCE (CAST('' AS VARCHAR(50)),'')  AS "MARCA4", -- VARCHAR(50)
            COALESCE (CAST('' AS VARCHAR(50)),'')  AS "MARCA5", -- VARCHAR(50)
            COALESCE (CAST('1' AS VARCHAR(2)),'')  AS "TIPOPAGO", -- CHAR(2)
            coalesce(CAST(USU."CodigoUsuario" AS VARCHAR(15)),'')  AS "USUARIOCREADOR", -- VARCHAR(15)
            LEFT(COALESCE (OPE."FechaCreacion" , '1900-01-01' ),10)  AS "FECHACREACION", -- DATETIME
            COALESCE (CAST('' AS VARCHAR(16)),'')  AS "TERMINAL", -- CHAR(16)
            COALESCE ( 0 , 0 )  AS "CODEVENTOTRANS", -- INT
            COALESCE (CAST(CASE WHEN "IdSpotAsociado" > 0 THEN 'S' ELSE 'N' END AS VARCHAR(1)),'')  AS "INDASOCIASPOT", -- CHAR(1)
            COALESCE (SINFO."MontoMonedaSecundariaIngreso" , 0 )  AS "MTOMONSECUINI", -- DECIMAL
            COALESCE (OPE."MontoMargen" , 0 )  AS "MARGEN", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CODSUBLIBRO", -- INT
            COALESCE ( 0 , 0 )  AS "CODLIBRO1", -- INT
            COALESCE ( 0 , 0 )  AS "CODSUBLIBRO1", -- INT
            COALESCE ( 0 , 0 )  AS "CODSUBSUBLIBRO", -- INT
            
            TO_VARCHAR (CASE WHEN OPE."FechaLiquidacion" < '1900-01-01' THEN '1900-01-01' ELSE OPE."FechaLiquidacion" END ,'YYYY-MM-DD')  AS "FECEFECTIVA", -- DATETIME
            
            
            COALESCE ( 0 , 0 )  AS "ANTPLAZO", -- INT
            COALESCE ( 0 , 0 )  AS "ANTPARMP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARCLP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARMPCOS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARMSCOS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPTOPARMP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPTOPARMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPTOPARMPCOS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPTOPARMSCOS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTTASAMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARMPENTFIS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARMSENTFIS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPARMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTCODMONCMP", -- INT
            COALESCE ( 0 , 0 )  AS "ANTPARMONCMP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTPOT10AJUSTE", -- INT
            COALESCE ( 0 , 0 )  AS "ANTEFEVALPTEUNIMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMSPOTMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMCLP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMLIQUIDARMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMCOSTOMS", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMCOSTOCLP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMAJUSTEMONCMP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTMAJUSTECLP", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMARGENCLP", -- DECIMAL
            COALESCE (SINFO."EGRMontoLiquidacion" , 0 )  AS "MTOLIQUIDACION", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "ANTMTOMONSECU", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "INDFWDAMERICANO", -- CHAR(1)
			CASE
				WHEN SINFO."IdEstado" = 'LIQ' AND SINFO."EGRIdMonedaLiquidacion" != '' THEN SINFO."EGRIdMonedaLiquidacion"
				ELSE 'CLP' END													AS "IDMONEDALIQUIDACION"
        	FROM Cartera as SINFO
			INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE ON OPE."IdOperacion" = SINFO."IdOperacion" AND OPE."IdTipoProducto" = SINFO."IdTipoProducto"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = OPE."IdCliente"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON SINFO."IdEmpresa" = EMP."IdEmpresa"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND por."IdPortafolio"  = OPE."IdPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = SINFO."IdUsuario"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG ON AG."IdPersona" = SINFO."IdAgente"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON ON MON."IdMoneda" = SINFO."IdMonedaPrincipal"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONS ON MONS."IdMoneda" = SINFO."IdMonedaSecundaria"
			LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PAR ON OPE."IdModalidadLiquidacion" = PAR."IdParametro" AND PAR."NombreTipoParametro" = 'ModalidadLiquidacion'

			WHERE SINFO.RANK = 1
			--WHERE OPE."FolioOperacion" = 23348

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMMovtosForward" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
