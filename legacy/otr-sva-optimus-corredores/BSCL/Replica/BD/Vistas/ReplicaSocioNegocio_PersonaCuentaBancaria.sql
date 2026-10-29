SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCuentaBancaria"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCuentaBancaria" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCuentaBancaria"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCuentaBancaria"  AS "IDCUENTABANCARIA", -- BIGINT
                  "IdBanco"  AS "IDBANCO", -- BIGINT
                  "IdTipoCuentaBancaria"  AS "IDTIPOCUENTABANCARIA", -- VARCHAR(4)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "NumeroCuentaBancaria"  AS "NUMEROCUENTABANCARIA", -- VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "FechaIngreso"  AS "FECHAINGRESO", -- VARCHAR(19)
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreTipoCuentaBancaria"  AS "NOMBRETIPOCUENTABANCARIA", -- VARCHAR(100)
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "NombreBanco"  AS "NOMBREBANCO", -- VARCHAR(100)
                  "CodigoBanco"  AS "CODIGOBANCO", -- VARCHAR(40)
                  "NombrePais"  AS "NOMBREPAIS", -- VARCHAR(100)
                  "CodigoAba" AS "CODIGOABA", -- VARCHAR(50)
                  "CodigoBancoExtranjero" AS "CODIGOBANCOEXT", -- VARCHAR(50)
				  "CodigoIban" AS "CODIGOIBAN", -- VARCHAR(50)
				  "CodigoCuentaExtranjero" AS "CODIGOCUENTAEXT", -- VARCHAR(50)
                  "NombreCompleto"  AS "NOMBRECOMPLETO" -- VARCHAR(302)

        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaCuentaBancaria' AND WST."Key" = "IdCuentaBancaria"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaCuentaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
