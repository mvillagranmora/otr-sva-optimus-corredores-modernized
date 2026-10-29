SELECT
     CPO."IdComprobantePagoOperacion"
    ,CPO."IdOperacion"
    ,CAR."IdTipoOperacion"
    ,CPO."IdFormaDePago"
    ,CPO."RecibePaga"
    ,CPO."IdMoneda"
    ,CPO."Monto"
    ,CPO."AfectarTesoreria"
    ,CASE WHEN CPO."RecibePaga" = 'P'  THEN
	 	COALESCE(PrefPort."IdCuentaBancaria",0) --(P)por defecto cliente
	ELSE		        			
		COALESCE(CuentaBanco."IdCuentaBanco",0) --(R)por defecto corredora		        			
	END AS "IdCuentaBancaria"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	ON (CPO."IdOperacion" = CAR."IdCartera" AND
		CPO."IdTipoProducto" = CAR."IdTipoProducto" AND
		CPO."IdEmpresa" = CAR."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FPAG
    ON (REPLACE(CPO."IdFormaDePago",'FWD', 'SPT') = FPAG."IdFormaDePago" AND
        FPAG."IdTipoProducto" = 'MEME' AND
        CPO."IdMoneda"= FPAG."IdMoneda" AND
        FPAG."IdEstado"='ACT')
LEFT JOIN (	SELECT pp."IdPortafolio",  ppa."IdTipoOperacion", ppa."IdMoneda", pp."IdCuentaBancaria"
			FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" pp 
			INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as ppa 
			ON pp."IdPreferenciaPago" = ppa."IdPreferenciaPago"
			WHERE pp."PorDefecto"= 'S') AS PrefPort 
	ON (PrefPort."IdPortafolio" = CAR."IdPortafolio"
		AND PrefPort."IdTipoOperacion" = REPLACE(FPAG."IdTipoOperacion", 'FWD', 'SPT')
		AND PrefPort."IdMoneda" = CPO."IdMoneda")
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES 
	ON (MES."IdTipoProducto" = 'MEME'
		AND MES."IdEstadoRegistro" = 'ACT')
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PrefPortCorr
   ON (PrefPortCorr."IdPortafolio" = MES."IdPortafolio" 
	   AND PrefPortCorr."IdPreferenciaPago" = FPAG."IdPreferenciaPago" 
	   AND PrefPortCorr."IdEmpresa" = MES."IdEmpresa"
	   AND PrefPortCorr."IdEstado" = 'ACT'
	   AND PrefPortCorr."PorDefecto" = 'S')
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CORCLI 
	ON MES."IdCliente" = CORCLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancariaCorr 
	ON (CuentaBancariaCorr."IdPersona" = CORCLI."IdPersona"
   		AND PrefPortCorr."IdCuentaBancaria" = CuentaBancariaCorr."IdCuentaBancaria")
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CuentaBanco 
  	ON (CuentaBanco."NumeroCuentaBancaria" = CuentaBancariaCorr."NumeroCuentaBancaria" 
	  	AND CuentaBanco."IdBanco" = CuentaBancariaCorr."IdBanco" 
	  	AND CuentaBanco."IdMoneda" = CuentaBancariaCorr."IdMoneda" 
	  	AND CuentaBanco."IdEstado" = 'ACT')
WHERE CPO."IdOperacion" IN (:IdCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEmpresa" = :IdEmpresa;