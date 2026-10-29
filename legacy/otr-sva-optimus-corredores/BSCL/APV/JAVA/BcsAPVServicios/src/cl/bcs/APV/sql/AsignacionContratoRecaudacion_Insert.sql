INSERT INTO "BSCL.APV.BD::REC.AsignacionContratoRecaudacion"(
	 "IdRecaudacionCliente"	--  BIGINT			NOT NULL,
	,"IdContratoSAAPV"	    --  BIGINT			NOT NULL,
 	,"FechaCreacion"         --  SECONDDATE		NOT NULL,
 	,"FechaModificacion"     --  SECONDDATE		NOT NULL,
)
VALUES(
	 :IdRecaudacionCliente
	,:IdContratoSAAPV
	,:FechaCreacion
	,:FechaModificacion
);