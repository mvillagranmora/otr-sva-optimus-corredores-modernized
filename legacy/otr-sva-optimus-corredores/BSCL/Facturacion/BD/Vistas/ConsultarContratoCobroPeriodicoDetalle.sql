SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodicoDetalle";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodicoDetalle" AS

SELECT 
	Cmbte."IdComprobante"
	 ,Cont."IdContratoComision" 				AS "IdContratoCobroPeriodico"
	 ,ContDet."IdDetalleContratoComision" 	AS "IdContratoCobroPeriodicoDetalle"
	 ,Cont."IdClasificacionTipoInstrumento"
	 ,ClaTipIns."Nombre" 					AS "ClasificacionTipoInstrumento"
	 ,COALESCE (ContDet."IdConcepto",'') AS "IdConceptoCobroPeriodico"
	 ,Concep."Nombre"						AS "Concepto"
	 ,ContDet."IdCobro"
	 ,cob."NombreCobro"
	 ,ContDet."EsPorRangos"
	 ,ContDet."MontoMinimo"
	 ,ContDet."MontoMaximo"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
	INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" Cont ON (Cmbte."IdCliente" = Cont."IdCliente" AND Cmbte."IdClasificacionTipoInstrumento" = Cont."IdClasificacionTipoInstrumento") 
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" ContDet ON (Cont."IdContratoComision" = ContDet."IdContratoComision")
	INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ClaTipIns ON (Cont."IdClasificacionTipoInstrumento" = ClaTipIns."IdClasificacionTipoInstrumento")
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" Concep ON (ContDet."IdConcepto" = Concep."IdConcepto")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" cob ON (ContDet."IdCobro" = cob."IdCobro")
	
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodicoDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;                                       