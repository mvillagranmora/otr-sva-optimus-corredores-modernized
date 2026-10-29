SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_MandatoPAC"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_MandatoPAC" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdMandatoPAC")  AS TABLE_KEY,
        		"Action" AS ACTION,
				"IdMandatoPAC" AS "IDMANDATOPAC",
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
				"NombreCompleto" AS "NOMBRECOMPLETO",
				"FolioCSC" AS "FOLIOCSC",
				"FolioSAAPV" AS "FOLIOSAAPV",
				"Receptor" AS "RECEPTOR",
				"Fecha" AS "FECHA",
				"NroMandato" AS "NROMANDATO",
				"Firma" AS "FIRMA",
				"Estado" AS "ESTADO",
				"IdPortafolio" AS "IDPORTAFOLIO",
				"NombrePlaza" AS "NOMBREPLAZA",
				"NombreMoneda" AS "NOMBREMONEDA",
				"IdCuentaBancaria" AS "IDCUENTABANCARIA",
				"NombreTipoCtaBancaria" AS "NOMBRETIPOCTABANCARIA",
				"NombreBanco" AS "NOMBREBANCO",
				"NumeroCtaBancaria" AS "NUMEROCTABANCARIA",
				"EstadoCliente" AS "ESTADOCLIENTE",
				"Canal" AS "CANAL",
				"EstadoCargo" AS "ESTADOCARGO",
				"MontoPesos" AS "MONTOPESOS",
				"MontoUF" AS "MONTOUF",
				"FolioPACMandatoAgrupador" AS "FOLIOPACMANDATOAGRUPADOR"

        from "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPACAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Tesoreria_MandatoPAC' AND WST."Key" = "IdMandatoPAC"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTesoreria_MandatoPAC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
