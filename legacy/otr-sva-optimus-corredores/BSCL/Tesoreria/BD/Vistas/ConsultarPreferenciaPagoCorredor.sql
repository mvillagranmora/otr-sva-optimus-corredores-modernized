SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS

SELECT
	 PRE."IdPreferenciasPagoCorredor"
	,PRE."IdTipoMovimiento"
	,PRE."IdConcepto"
	,CON."Nombre" AS "NombreConcepto"
	,PRE."IdMedioDePago"
	,MPM."IdModalidadDePago"
	,PRE."IdCuentaBanco"
	,PRE."UnicaOpcionDePago"
	,PRE."IdEstado"
	,PRE."IdEmpresa"
	,EST."Nombre" AS "NombreEstadoPreferencia"
	,PRE."NombreEntidad"
	,PRE."CodigoTipoCargaMasiva"
	,PRE."IdPersonaEntidadRecaudadora" -- para recaudación ej. un banco/servipag
	,PER."NombreCompleto" AS "NombreEntidadRecaudadora"
	,PER."DocumentoIdentidad" AS "DocumentoIdentidadEntidadRecaudadora"
	,BAN."NombreBanco"
	
	,PRE."ProfundidadProceso" -- profundidar en la generacion de mov/com/liq
	,PRE."IdMoneda"
	,MON."Nombre" AS "NombreMoneda"
	,PRE."EditableEnProceso" -- editable en complemento de carga masiva 
	,MED."Nombre" AS "NombreMedioDePago"
	
	-- Datos del banco
	,BCO."IdBanco"
	,BCO."NombreCuentaBancaria"
	,BCO."IdTipoCuentaBancaria"
	,TCU."NombreTipoCuentaBancaria"	
	,BCO."IdMoneda" AS "IdMonedaCuentaBanco"
	,BCO."NumeroCuentaBancaria"
	,BCO."IdEstado" AS "IdEstadoCuentaBanco"
	,BCO."GeneraNomina"
	,BCO."GeneraTransferencia"	

FROM "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" AS PRE
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (PRE."IdEstado" = EST."IdEstado")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (PRE."IdConcepto" = CON."IdConcepto")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MPM ON (MPM."IdMedioDePago" = PRE."IdMedioDePago")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MED ON (MED."IdMedioDePago" = PRE."IdMedioDePago")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" BCO ON (BCO."IdCuentaBanco" = PRE."IdCuentaBanco")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = PRE."IdPersonaEntidadRecaudadora")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = PRE."IdMoneda")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA ON (CTA."IdCuentaBanco" = PRE."IdCuentaBanco")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (BAN."IdBanco" = CTA."IdBanco")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCU ON (TCU."IdTipoCuentaBancaria" = BCO."IdTipoCuentaBancaria")
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


