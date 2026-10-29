/**
 * Proposito: Consulta las preferencias de pago de la corredora
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 

SELECT
	 "IdPreferenciasPagoCorredor"
	,"IdTipoMovimiento"
	,"IdConcepto"
	,"IdMedioDePago"
	,"IdModalidadDePago"
	,"IdCuentaBanco"
	,"IdEstado"
	,"IdBanco"
	,"NombreCuentaBancaria"
	,"IdTipoCuentaBancaria"
	,"IdMonedaCuentaBanco"
	,"NumeroCuentaBancaria"
	,"IdEstadoCuentaBanco"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor"
WHERE  "IdEstado"         = 'ACT'
AND    "IdEmpresa"        = :IdEmpresa
AND    "IdConcepto"       IN (:IdConceptos)
AND    "IdTipoMovimiento" IN (:IdTipoMovimientos);

