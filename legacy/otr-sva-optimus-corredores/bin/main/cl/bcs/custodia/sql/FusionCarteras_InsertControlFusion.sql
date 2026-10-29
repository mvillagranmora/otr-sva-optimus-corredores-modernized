INSERT INTO "BSCL.Custodia.BD::HIS.ControlFusionCartera"
("IdControlFusion", "IdUsuario", "IdCliente", "IdPortafolio", "IdEstado", "Resultado", "FechaOperacion")
VALUES
(:IdControlFusion, :IdUsuario, :IdCliente, :IdPortafolio, :IdEstado, :Resultado, CURRENT_DATE);