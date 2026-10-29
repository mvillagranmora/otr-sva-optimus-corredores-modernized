SELECT COMP."IdOperacion"
	   ,CASE WHEN COMP."RecibePaga" = 'P' THEN 'MOVEGR' WHEN COMP."RecibePaga" = 'R' THEN 'MOVING' END AS "IdTipoMovimiento"
		,FPAG."IdMedioDePago"
		,COMP."IdModalidadPago"
		,COMP."IdMoneda"
		,COMP."Monto"
		,TO_VARCHAR(COMP."FechaLiquidacionComprobantePagoOperacion",'YYYY-MM-DD') AS "FechaLiquidacionComprobantePagoOperacion"
		,COMP."AfectarTesoreria"
		,COMP."IdFormaDePago"
		,FPAG."IdCondicionDeLiquidacion"		
        ,CASE
           WHEN SolAporRes."IdTipoCanalIngreso" = 'WEB' OR SPT."IdTipoCanalIngreso" = 'AGE' OR SPT."IdTipoCanalIngreso" = 'FWD' THEN COMP."IdCuentaBancaria"
        ELSE CASE WHEN COALESCE(COMP."IdCuentaBancaria",0) <> 0 THEN COMP."IdCuentaBancaria" ELSE 
		        CASE WHEN SPT."IdTipoOperacion" = 'VENSPT'  THEN 
		            	CASE WHEN COMP."RecibePaga" = 'P'  THEN
		            		CASE WHEN SPT."IdTipoCanalIngreso" = 'DAT' THEN 
		        				COALESCE(CuentaBanco."IdCuentaBanco",0)
		        			ELSE
		             			COALESCE(PrefPortCorr."IdCuentaBancaria",0)  --(P)por defecto corredora
		             		END
		        		ELSE 
		        			COALESCE(PrefPort."IdCuentaBancaria",0)   --(R)por defecto cliente
		        		END
		  		ELSE 
		  				CASE WHEN COMP."RecibePaga" = 'P'  THEN
		             		COALESCE(PrefPort."IdCuentaBancaria",0) --(P)por defecto cliente
		        		ELSE
		        			CASE WHEN SPT."IdTipoCanalIngreso" = 'DAT' THEN 
		        				COALESCE(CuentaBanco."IdCuentaBanco",0)
		        			ELSE
		        			 COALESCE(PrefPortCorr."IdCuentaBancaria",0)  --(R)por defecto corredora
		        			END
		        		END
		        END
        END END AS "IdCuentaBancaria"
        ,CASE 
           WHEN SolAporRes."IdTipoCanalIngreso" = 'WEB' AND COMP."IdCuentaBancaria" = 0 THEN ''
           WHEN SolAporRes."IdTipoCanalIngreso" = 'WEB' OR SPT."IdTipoCanalIngreso" = 'AGE' OR SPT."IdTipoCanalIngreso" = 'FWD' THEN COALESCE(COMP."NumeroCuentaBancaria",'')
        ELSE CASE WHEN COALESCE(COMP."NumeroCuentaBancaria",'') <> '' THEN COMP."NumeroCuentaBancaria" ELSE 
			        CASE WHEN SPT."IdTipoOperacion" = 'VENSPT'  THEN 
			            	CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancariaCorr."NumeroCuentaBancaria",'') --(P)por defecto corredora
			        		ELSE 
			        			COALESCE(CuentaBancaria."NumeroCuentaBancaria",'')  --(R)por defecto cliente
			        		END
			  		ELSE 
			  				CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancaria."NumeroCuentaBancaria",'') --(P)por defecto cliente
			        		ELSE 
			        			COALESCE(CuentaBancariaCorr."NumeroCuentaBancaria",'')  --(R)por defecto corredora
			        		END
			        END
	        END
         END AS "NumeroCuentaBancaria"
        ,CASE
           WHEN SolAporRes."IdTipoCanalIngreso" = 'WEB' OR SPT."IdTipoCanalIngreso" = 'AGE' OR SPT."IdTipoCanalIngreso" = 'FWD' THEN COMP."IdTipoCuentaBancaria"
        ELSE CASE WHEN COALESCE(COMP."IdTipoCuentaBancaria",'') <> '' THEN COMP."IdTipoCuentaBancaria" ELSE 
			        CASE WHEN SPT."IdTipoOperacion" = 'VENSPT'  THEN 
			            	CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancariaCorr."IdTipoCuentaBancaria",'') --(P)por defecto corredora
			        		ELSE 
			        			COALESCE(CuentaBancaria."IdTipoCuentaBancaria",'')  --(R)por defecto cliente
			        		END
			  		ELSE 
			  				CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancaria."IdTipoCuentaBancaria",'') --(P)por defecto cliente
			        		ELSE 
			        			COALESCE(CuentaBancariaCorr."IdTipoCuentaBancaria",'')  --(R)por defecto corredora
			        		END
			        END
	        END
        END AS "IdTipoCuentaBancaria"
        ,CASE
           WHEN SolAporRes."IdTipoCanalIngreso" = 'WEB' OR SPT."IdTipoCanalIngreso" = 'AGE' OR SPT."IdTipoCanalIngreso" = 'FWD' THEN COMP."IdBanco"
        ELSE CASE WHEN COALESCE(COMP."IdBanco",0) <> 0 THEN COMP."IdBanco" ELSE 
			        CASE WHEN SPT."IdTipoOperacion" = 'VENSPT'  THEN 
			            	CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancariaCorr."IdBanco",0) --(P)por defecto corredora
			        		ELSE 
			        			COALESCE(CuentaBancaria."IdBanco",0)  --(R)por defecto cliente
			        		END
			  		ELSE 
			  				CASE WHEN COMP."RecibePaga" = 'P'  THEN
			             		COALESCE(CuentaBancaria."IdBanco",0) --(P)por defecto cliente
			        		ELSE 
			        			COALESCE(CuentaBancariaCorr."IdBanco",0)  --(R)por defecto corredora
			        		END
			        END
	        END
        END AS "IdBanco"
FROM 	"BSCL.Mesa.BD::SPT.OperacionSpot" SPT
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolAporRes
     ON SolAporRes."IdSolicitudAporteRescate" = SPT."IdSolicitudAporteRescate"
    AND SolAporRes."Estado" = 'CRT'
		INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" COMP
		ON	comp."IdOperacion" = SPT."IdOperacionSpot" 
		INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FPAG
		ON (FPAG."IdFormaDePago" = COMP."IdFormaDePago" AND
			FPAG."IdMoneda" = COMP."IdMoneda" AND
			FPAG."IdTipoProducto" = COMP."IdTipoProducto" AND
			FPAG."IdEstado" = 'ACT' AND FPAG."IdTipoProducto" = 'MEME')
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
    ON Cliente."IdCliente" = SPT."IdCliente"			
  LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PrefPort 
    ON PrefPort."IdPortafolio" = SPT."IdPortafolio" 
   AND PrefPort."IdPreferenciaPago" = FPAG."IdPreferenciaPago" 
   AND PrefPort."IdEmpresa" = SPT."IdEmpresa"
   AND PrefPort."IdEstado" = 'ACT'
   AND PrefPort."PorDefecto" = 'S'
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancaria 
    ON CuentaBancaria."IdPersona" = Cliente."IdPersona"
   AND PrefPort."IdCuentaBancaria" = CuentaBancaria."IdCuentaBancaria"
   LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON SPT."IdMesa" = MES."IdMesa" 
  LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PrefPortCorr
    ON PrefPortCorr."IdPortafolio" = MES."IdPortafolio" 
   AND PrefPortCorr."IdPreferenciaPago" = FPAG."IdPreferenciaPago" 
   AND PrefPortCorr."IdEmpresa" = MES."IdEmpresa"
   AND PrefPortCorr."IdEstado" = 'ACT'
   AND PrefPortCorr."PorDefecto" = 'S'
  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CORCLI ON MES."IdCliente" = CORCLI."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancariaCorr 
    ON CuentaBancariaCorr."IdPersona" = CORCLI."IdPersona"
   AND PrefPortCorr."IdCuentaBancaria" = CuentaBancariaCorr."IdCuentaBancaria"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CuentaBanco 
  	ON CuentaBanco."NumeroCuentaBancaria" = CuentaBancariaCorr."NumeroCuentaBancaria" 
  	AND CuentaBanco."IdBanco" = CuentaBancariaCorr."IdBanco" 
  	AND CuentaBanco."IdMoneda" = CuentaBancariaCorr."IdMoneda" 
  	AND CuentaBanco."IdEstado" = 'ACT'
WHERE 	SPT."IdOperacionSpot" IN (:NumeroOperacion) 
  AND 	SPT."IdEmpresa" = :IdEmpresa;  