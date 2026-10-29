SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPreferenciaPagoPortafolioSpot";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPreferenciaPagoPortafolioSpot"
AS 
	SELECT 
	PPP."IdPreferenciaPagoPortafolio",
    CONCAT(PPP."IdPreferenciaPagoPortafolio",CONCAT('-',FPMESA."Dias")) AS "IdPreferenciaPagoPortafolioKey",
	PPP."IdPortafolio", 
	PPP."IdPreferenciaPago",
	FPMESA."Dias",
	PPP."IdCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN 0  
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."IdBanco",0)
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(CBANC."IdBanco",0) ELSE COALESCE(CBANCCOR."IdBanco",0) END
				ELSE 0 END) 
		END as "IdBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(BANCBANC."NombreBanco",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(BANCBANC."NombreBanco",'') ELSE COALESCE(CBANCCOR."NombreBanco",'') END 					
				ELSE '' END) 
		END as "NombreBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."NumeroCuentaBancaria",'')  	
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE COALESCE(CBANCCOR."NumeroCuentaBancaria",'') END			
				ELSE '' END) 
		END as "NumeroCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'') ELSE COALESCE(CBANCCOR."IdTipoCuentaBancaria",'') END			
				ELSE '' END) 
		END as "IdTipoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') 
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" = 'MEME' OR TIPOP."IdTipoProducto" = 'MERF' OR TIPOP."IdTipoProducto" = 'MEFW' THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') ELSE COALESCE(CBANCCOR."NombreTipoCuentaBancaria",'') END  	
				ELSE '' END) 
		END as "NombreTipoCuentaBancaria",
	PPP."IdTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN 0 ELSE COALESCE(TBANC."IdBanco",0) END as "IdBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(BANTBANC."NombreBanco",'') END as "NombreBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TBANC."NumeroTarjetaBancaria",'') END as "NumeroTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."IdTipoTarjetaBancaria",'') END as "IdTipoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."NombreTipoTarjetaBancaria",'') END as "NombreTipoTarjetaBancaria",
   	PPP."FechaCreacion",
	PPP."FechaModificacion",
	PPP."IdUsuarioCreador",
	PPP."IdEstado",
	ESTPPP."Nombre" as "Estado",
	PPP."IdEmpresa",
	PP."IdTipoOperacion", 	-- (X)
	TIPOP."Nombre" as "TipoOperacion",
	PP."IdMedioDePago",
	MP."Nombre" as "MedioDePago",
	CASE WHEN MP."UsaCuentaBancaria" = 'S' AND BANCBANC."NombreBanco" IS NOT NULL AND CBANC."NumeroCuentaBancaria" IS NOT NULL
		THEN CONCAT(COALESCE(CASE PP."IdMedioDePago" WHEN 'TRA' THEN CONCAT(CONLIQ."IdCondicionDeLiquidacion",CONCAT(' - ',FPMESA."Nombre")) ELSE FPMESA."Nombre" END,''),CONCAT('-',CONCAT(COALESCE(BANCBANC."NombreBanco",''),CONCAT('-',COALESCE(CBANC."NumeroCuentaBancaria",''))))) 
		ELSE CASE PP."IdMedioDePago" WHEN 'TRA' THEN CONCAT(CONLIQ."IdCondicionDeLiquidacion",CONCAT(' - ',FPMESA."Nombre")) ELSE FPMESA."Nombre" END END AS "Descripcion",
	PP."IdMoneda",			-- (X)
	PP."UsaPersonaRelacionada",
	CASE PP."UsaPersonaRelacionada" WHEN 'S' THEN 'Si' WHEN 'N' THEN 'No' ELSE '-' END AS "TextoUsaPersonaRelacionada",
	PPP."IdPersonaRelacionada" as "IdPersonaRelacionada",
	COALESCE(PERSREL."NombreCompleto",'') as "NombrePersonaRelacionada",
	COALESCE(PERSREL."DocumentoIdentidad",'') as "DocumentoIdentidadPersonaRelacionada",
	CASE PPP."IdPersonaRelacionada" WHEN 0 THEN '' ELSE CONCAT(CONCAT(COALESCE(PERSREL."NombreCompleto",''),' - '),COALESCE(PERSREL."DocumentoIdentidad",'')) END as "TextoPersonaRelacionada",
	MP."UsaCuentaBancaria",
	CASE MP."UsaCuentaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaCuentaBancaria",
	MP."UsaTarjetaBancaria",
	CASE MP."UsaTarjetaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaTarjetaBancaria",
	TIPOP."RecibePagaPortafolio",
	CASE TIPOP."RecibePagaPortafolio" WHEN 'R' THEN 'Recibe' WHEN 'P' THEN 'Paga' ELSE '-' END AS "TextoRecibePagaPortafolio",
	CASE CONFME."Valor" WHEN 'N' THEN 
					CASE FPMESA."Dias" WHEN 0 THEN PPP."PorDefecto" ELSE 'N' END
			ELSE
				CASE PPP."PorDefecto" WHEN 'S'
					THEN FPMESA."PorDefecto" 
					ELSE PPP."PorDefecto" END
				END AS "PorDefecto",
		CASE CONFME."Valor" WHEN 'N' THEN 
					CASE FPMESA."Dias" WHEN 0 THEN PPP."PorDefecto" ELSE 'N' END
			ELSE
				CASE PPP."PorDefecto" WHEN 'S'
					THEN FPMESA."PorDefecto" 
					ELSE PPP."PorDefecto" END
				END AS "TextoPorDefecto",
	CASE 
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'R' THEN MP."Nombre" || ', ' || COALESCE(BANCBANC."NombreBanco",'') || ' - ' || COALESCE(CBANC."NumeroCuentaBancaria",'')
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'P' THEN MP."Nombre" || ', ' || COALESCE(CBANCCOR."NombreBanco",'') || ' - ' || COALESCE(CBANCCOR."NumeroCuentaBancaria",'')
		WHEN MP."UsaTarjetaBancaria" = 'S' THEN MP."Nombre" || ', ' || COALESCE(BANTBANC."NombreBanco",'') || ' - ' || COALESCE(TBANC."NumeroTarjetaBancaria",'')
		ELSE MP."Nombre"
		END as "TextoPreferenciaPago",
	BANTBANC."IdBanco" AS "IdBancoTarjetaBancariaBancaria",
	TIPOP."IdTipoProducto",
	POR."IdCliente",
	POR."IdMatrizAgrupadorAPV",
    FPMESA."Nombre" AS "NombreFormaPago",
    FPMESA."IdFormaDePago",
	CONLIQ."IdCondicionDeLiquidacion" 
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP
	INNER JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" AS FPMESA ON FPMESA."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND PPP."IdEmpresa" = FPMESA."IdEmpresa" AND PPP."IdEstado" = FPMESA."IdEstado"
  INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
  INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
  INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MP ON PP."IdMedioDePago" = MP."IdMedioDePago"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON PPP."IdPortafolio" = POR."IdPortafolio"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CBANCCOR ON PPP."IdCuentaBancaria" = CBANCCOR."IdCuentaBanco"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" as TBANC ON CLI."IdPersona" = TBANC."IdPersona" AND PPP."IdTarjetaBancaria" = TBANC."IdTarjetaBancaria"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSREL ON PERSREL."IdPersona" = PPP."IdPersonaRelacionada"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON BANCBANC."IdBanco" = CBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANTBANC ON BANTBANC."IdBanco" = TBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" as TCBANC ON CBANC."IdTipoCuentaBancaria" = TCBANC."IdTipoCuentaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" as TTBANC ON TBANC."IdTipoTarjetaBancaria" = TTBANC."IdTipoTarjetaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTPPP ON ESTPPP."IdEstado" = PPP."IdEstado"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CONLIQ on CONLIQ."PlazoEnDias" = FPMESA."Dias"  
  LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" as CONFME on CONFME."CodigoParametro" = 'TOMAPORDEFECTODIASVALUTAMESA'
WHERE PPP."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarPreferenciaPagoPortafolioSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 