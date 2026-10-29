SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotas"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotas" AS

        Select WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdFolioCSC") AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdFolioCSC" AS "IDFOLIOCSC",
				"Folio" AS "FOLIO",
				"Fecha" AS "FECHA",
				"MontoPesos" AS "MONTOPESOS",
				"MontoUF" AS "MONTOUF",
				"TipoPago" AS "TIPOPAGO",
				"NombreMedioPago" AS "NOMBREMEDIOPAGO",
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
				"CodigoPortafolio" AS "CODIGOPORTAFOLIO",
				"NombreCompleto" AS "NOMBRECOMPLETO",
				"NombreBanco" AS "NOMBREBANCO",
				"IdEstado" AS "IDESTADO",
				"Estado" AS "ESTADO",
				"DiaDescuento" AS "DIADESCUENTO",
				"FolioRelacionado" AS "FOLIORELACIONADO",
				"Hora"  AS "HORA",
				"IdCliente" AS "IDCLIENTE",
				"IdPortafolio" AS "IDPORTAFOLIO"


        from "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_ContratoSuscripcionCuotas' AND WST."Key" = "IdFolioCSC"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
