SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotasDetalle"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotasDetalle" AS

        Select WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdFolioCSC")  || ':' || TO_VARCHAR("IdCSCDetalle") AS TABLE_KEY,
                "Action" AS ACTION,
				"IdCSCDetalle" as "IDCSCDETALLE",
				"IdFolioCSC" as "IDFOLIOCSC",
				"IdSerieFondo" as "IDSERIEFONDO",
				"IdFondo" as "IDFONDO",
				"Regimen" as "REGIMEN",
				"Monto" as "MONTO",
				"Porcentaje" as "PORCENTAJE"

        from "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalleAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
        			ON  WST."Table" = 'FondoMutuo_ContratoSuscripcionCuotasDetalle' AND WST."Key" = TO_VARCHAR("IdFolioCSC")  || ':' || TO_VARCHAR("IdCSCDetalle")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ContratoSuscripcionCuotas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
