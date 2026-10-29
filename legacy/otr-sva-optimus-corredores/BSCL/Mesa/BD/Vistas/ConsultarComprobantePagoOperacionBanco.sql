SET SCHEMA "BSCL";
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacionBanco";
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacionBanco"
AS 
SELECT 
	CPAG."IdComprobantePagoOperacion" ,
	CPAG."IdEmpresa" ,
	CPAG."IdOperacion" ,
	CPAG."IdIngresoEgreso" ,
	CPAG."IdAbono" ,
	CPAG."IdCargo" ,
	CPAG."IdTipoProducto" ,
	CPAG."IdFormaDePago" ,
	CPAG."RecibePaga" ,
	CASE
		WHEN CPAG."RecibePaga" = 'R' THEN 'Recibimos'
		WHEN CPAG."RecibePaga" = 'P' THEN 'Entregamos'
		ELSE ''
	END AS "RecibePagaDesc",
	CPAG."IdModalidadPago" ,
	CPAG."IdMoneda" AS "IdMonedaComprobantePago",
	CPAG."Monto" ,
	CPAG."FechaIngresoComprobantePagoOperacion" ,
	CPAG."FechaLiquidacionComprobantePagoOperacion" ,
	CPAG."IdEstado" AS "IdEstadoComprobantePago",
	CPAG."AfectarTesoreria" ,
	MP."Nombre" AS "NombreFormaDePago",
	NULL AS "Dias",
	TIOP."RecibePagaPortafolio" ,
	CASE
		WHEN TIOP."RecibePagaPortafolio" = 'R' THEN 'Recibimos'
		WHEN TIOP."RecibePagaPortafolio" = 'P' THEN 'Entregamos'
		ELSE ''
	END AS "RecibePagaPortafolioDesc",
	PPAG."IdPreferenciaPago" ,
	PPAG."IdTipoOperacion" ,
	PPAG."IdMedioDePago" ,
	PPAG."IdMoneda" ,
	PPAG."UsaPersonaRelacionada" ,
	PPAG."FechaCreacion" ,
	PPAG."FechaModificacion" ,
	PPAG."IdUsuarioCreador" ,
	PPAG."IdEstado" ,
	TIOP."Nombre" AS "NombreTipoOperacion",
	TIPR."Nombre" AS "NombreTipoProducto",
	MP."Nombre"  AS "NombreMedioDePago",
	COALESCE((( MON."IdMoneda" || ' - ') || MON."Nombre") ,'-') as "NombreMoneda",
	MP."UsaCuentaBancaria" ,
	MP."UsaTarjetaBancaria" ,
	EST."Nombre" AS "NombreEstado",
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CBANCCORR."IdCuentaBanco",0) --CORREDOR
		ELSE
			COALESCE(CPAG."IdCuentaBanco", 0)--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CBANCCORR."IdCuentaBanco",0) --CORREDOR
		ELSE
			COALESCE(CPAG."IdCuentaBanco", 0)--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(CPAG."IdCuentaBanco", 0)--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CBANCCORR."IdCuentaBanco",0) --CORREDOR
	END END END END AS "IdCuentaBancaria", 
	--"IdBanco"
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CBANCCORR."IdBanco" ,0) --CORREDOR
		ELSE
			COALESCE(BAN."IdBanco", 0)--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CBANCCORR."IdBanco" ,0)--CORREDOR
		ELSE
			COALESCE(BAN."IdBanco", 0)--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(BAN."IdBanco", 0)--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CBANCCORR."IdBanco" ,0)--CORREDOR
	END END END END AS "IdBanco", 
	--"IdTipoCuentaBancaria"
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CBANCCORR."IdTipoCuentaBancaria" ,'') --CORREDOR
		ELSE
			COALESCE(TCBAN."NombreTipoCuentaBancaria", '')--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CBANCCORR."IdTipoCuentaBancaria" ,'')--CORREDOR
		ELSE
			COALESCE(TCBAN."NombreTipoCuentaBancaria", '')--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(TCBAN."NombreTipoCuentaBancaria", '')--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CBANCCORR."IdTipoCuentaBancaria" ,'')--CORREDOR
	END END END END AS "IdTipoCuentaBancaria", 
	--"NombreBancoCuentaBancaria"
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CORBANCBANC."NombreBanco" ,'') --CORREDOR
		ELSE
			COALESCE(BAN."NombreBanco", '')--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CORBANCBANC."NombreBanco" ,'') --CORREDOR
		ELSE
			COALESCE(BAN."NombreBanco", '')--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(BAN."NombreBanco", '') 		--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CORBANCBANC."NombreBanco" ,'') --CORREDOR
	END END END END AS "NombreBancoCuentaBancaria", 
	--"NumeroCuentaBancaria"
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CBANCCORR."NumeroCuentaBancaria" ,'')  --CORREDOR
		ELSE
			COALESCE(BANP."NumeroCuentaBancaria",'')--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CBANCCORR."NumeroCuentaBancaria" ,'')--CORREDOR
		ELSE
			COALESCE(BANP."NumeroCuentaBancaria",'')--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(BANP."NumeroCuentaBancaria",'')--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CBANCCORR."NumeroCuentaBancaria" ,'') --CORREDOR
	END END END END AS "NumeroCuentaBancaria",
	--por defecto
	CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'R' THEN 
			COALESCE(CORCBANC."Predeterminado" ,'')  --CORREDOR
		ELSE
			COALESCE(BANP."Predeterminado",'')--CLIENTE
		END
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN
		CASE WHEN CPAG."RecibePaga" = 'P' THEN 
			COALESCE(CORCBANC."Predeterminado" ,'')--CORREDOR
		ELSE
			COALESCE(BANP."Predeterminado",'')--CLIENTE
		END 
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'COMTER' THEN
		COALESCE(BANP."Predeterminado",'')--CLIENTE
	ELSE CASE WHEN OPE."IdTipoOperacion" = 'VENTER' THEN
		COALESCE(CORCBANC."Predeterminado" ,'') --CORREDOR
	END END END END AS "PorDefecto",
	OPE."IdTipoOperacion" AS "IdTipoOperacionBase"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPAG
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" BANP ON BANP."IdCuentaBancaria" = CPAG."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BAN ON BAN."IdBanco" = BANP."IdBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TCBAN ON TCBAN."IdTipoCuentaBancaria" = BANP."IdTipoCuentaBancaria" 
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PPAG ON PPAG."IdPreferenciaPago" = CPAG."IdFormaDePago" 
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" MP ON MP."IdMedioDePago" = PPAG."IdMedioDePago" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIOP ON TIOP."IdTipoOperacion" = PPAG."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TIPR ON TIPR."IdTipoProducto"  = TIOP."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON ON MON."IdMoneda" = PPAG."IdMoneda" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = PPAG."IdEstado" 
--corredora
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = CPAG."IdOperacion" 
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBANCCORR ON CBANCCORR."IdCuentaBanco" = CPAG."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as CORBANCBANC ON (CORBANCBANC."IdBanco" = CBANCCORR."IdBanco")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLICOR ON OPE."IdClienteCorredor" = CLICOR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CORCBANC ON CLICOR."IdPersona" = CORCBANC."IdPersona" AND  CORBANCBANC."IdBanco" = CORCBANC."IdBanco" 
  																AND	CBANCCORR."IdMoneda" = CORCBANC."IdMoneda"
  																AND	CBANCCORR."NumeroCuentaBancaria" = CORCBANC."NumeroCuentaBancaria" 
                                                                AND CORCBANC."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as CORPPP ON (OPE."IdPortafolioCorredor"=CORPPP."IdPortafolio" 
																			AND PPAG."IdPreferenciaPago" = CORPPP."IdPreferenciaPago"
																			AND CPAG."IdEmpresa" = CORPPP."IdEmpresa"
																			AND CORPPP."IdEstado" = 'ACT'
																			AND  CORPPP."IdCuentaBancaria" = CORCBANC."IdCuentaBancaria")
WHERE CPAG."IdTipoProducto" = 'MERF';
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacionBanco" TO BCS_DESARROLLADOR WITH GRANT OPTION; 