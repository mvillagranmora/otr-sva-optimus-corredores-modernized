SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarFormasDePagoMesaCliente";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarFormasDePagoMesaCliente" AS 
SELECT 
	PPP."IdPortafolio"
	, CASE WHEN PP."IdMedioDePago" = 'TRA' AND COALESCE(CBANC."IdCuentaBancaria",0) <> 0 THEN concat (FPMESA."IdFormaDePago" , concat ('_', CBANC."IdCuentaBancaria"))  ELSE FPMESA."IdFormaDePago" END AS "IdFormaDePago"
	, ( CASE WHEN PP."IdMedioDePago" = 'TRA' AND COALESCE(CBANC."IdCuentaBancaria",0) <> 0 THEN  concat (FPMESA."Nombre"  , concat ( ' - ', CBANC."NumeroCuentaBancaria"))  ELSE FPMESA."Nombre" END) as "NombreFormaDePago",
	( CASE WHEN PP."IdMedioDePago" = 'TRA' THEN  COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE '' END)   AS "NumeroCuentaBancaria",
	( CASE WHEN PP."IdMedioDePago" = 'TRA' THEN  COALESCE(BANCBANC."NombreBanco",'') ELSE '' END)  	AS "NombreBancoCuentaBancaria",
	FPMESA."Dias",
	PPP."IdPreferenciaPago",
	COALESCE(CBANC."IdCuentaBancaria",0) as "IdCuentaBancaria",
	PPP."FechaCreacion",
	PPP."FechaModificacion",
	PPP."IdUsuarioCreador",
	PPP."IdEstado",
	ESTPPP."Nombre" as "NombreEstado",
	PPP."IdEmpresa",
	PP."IdTipoOperacion", 	
	TIPOP."Nombre" as "NombreTipoOperacion",
	PP."IdMedioDePago",
	MP."Nombre" as "NombreMedioDePago",
	PP."IdMoneda",			
	MP."UsaCuentaBancaria",
	MP."UsaTarjetaBancaria",
	TIPOP."RecibePagaPortafolio",
	CASE TIPOP."RecibePagaPortafolio" WHEN 'R' THEN 'Recibe' WHEN 'P' THEN 'Paga' ELSE '-' END AS "RecibePagaPortafolioDesc",
	CASE WHEN  FPMESA."Dias" = 0 THEN PPP."PorDefecto" ELSE 'N' END AS "PorDefecto",
	TIPOP."IdTipoProducto"
FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa" AS FPMESA
  INNER JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP ON FPMESA."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND PPP."IdEmpresa" = FPMESA."IdEmpresa" AND PPP."IdEstado" = 'ACT'
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
  WHERE FPMESA."IdEstado" = 'ACT';
