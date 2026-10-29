SELECT
"IdTraspaso"
,"IdPersona"
,"IdEmpleador"
,"FolioContratoSAAPV"
,"MontoPesoDetalle"
,"Trabajador"
,"IdEstado"
,"NombreEntidad"
FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV"
WHERE "IdTraspaso" IN (:IdTraspaso)