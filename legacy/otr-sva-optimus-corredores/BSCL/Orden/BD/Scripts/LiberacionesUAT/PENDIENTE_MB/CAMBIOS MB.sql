SET SCHEMA "BSCL";
GO

--LISTO
--ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Asignacion" ADD ("Paridad" DECIMAL(26,8) NOT NULL DEFAULT 0);
--GO
--ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Asignacion" ADD ("FechaParidad" DATE NOT NULL DEFAULT '0001-01-01');
--GO
--ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Asignacion" ADD ("FechaTipoCambio" DATE NOT NULL DEFAULT '0001-01-01');
--GO

/*INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(202, 'CORREDORA', 'CONTVENCOR', 'INGRESOORDEN', 'RECHAZO', 'AGE', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(203, 'CORREDORA', 'CNOCRV', 'INGRESOORDEN', 'RECHAZO', 'TRA', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(204, 'CORREDORA', 'CNOCRV', 'INGRESOORDEN', 'RECHAZO', 'AGE', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(205, 'CORREDORA', 'CNOCRF', 'INGRESOORDEN', 'RECHAZO', 'TRA', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(206, 'CORREDORA', 'CNOCRF', 'INGRESOORDEN', 'RECHAZO', 'AGE', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(207, 'CORREDORA', 'VNOCRV', 'INGRESOORDEN', 'RECHAZO', 'TRA', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(208, 'CORREDORA', 'VNOCRV', 'INGRESOORDEN', 'RECHAZO', 'AGE', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(209, 'CORREDORA', 'VNOCRF', 'INGRESOORDEN', 'RECHAZO', 'TRA', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(210, 'CORREDORA', 'VNOCRF', 'INGRESOORDEN', 'RECHAZO', 'AGE', '0', '', '', '0', '0001-01-01 00:00:00', '0001-01-01 00:00:00', 1, 'ACT');

*/

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD CONSTRAINT "fk_Transaccion_LibroOrdenes" 
FOREIGN KEY("IdLibroOrdenes") REFERENCES "BSCL"."BSCL.Orden.BD::ORD.LibroOrdenes"("IdLibroOrdenes") ON DELETE RESTRICT;

UPDATE TRAN
SET "IdLibroOrdenes" = 18
FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" TRAN
WHERE "IdTransaccion" IN (
	SELECT "IdTransaccion" FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL"."BSCL.Orden.BD::ORD.LibroOrdenes" lib ON tran."IdLibroOrdenes" = lib."IdLibroOrdenes" 
WHERE lib."IdLibroOrdenes" IS null
)

COMMIT;




