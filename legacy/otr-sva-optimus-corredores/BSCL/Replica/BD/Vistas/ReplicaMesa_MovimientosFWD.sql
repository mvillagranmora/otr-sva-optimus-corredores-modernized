SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosFWD"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosFWD" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdOperacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdOperacion"  AS "IDOPERACION", -- BIGINT
                  "IdCartera"  AS "IDCARTERA", -- BIGINT
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "Modalidad"  AS "MODALIDAD", -- BIGINT
                  "NombreModalidad"  AS "NOMBREMODALIDAD", -- VARCHAR(100)
                  "IdMesa"  AS "IDMESA", -- BIGINT
                  "NombreMesa"  AS "NOMBREMESA", -- VARCHAR(30)
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(10)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "FechaVencimiento"  AS "FECHAVENCIMIENTO", -- VARCHAR(10)
                  "FechaEfectiva"  AS "FECHAEFECTIVA", -- VARCHAR(10)
                  "Plazo"  AS "PLAZO", -- BIGINT
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "DocumentoIdentidadCliente"  AS "DOCUMENTOIDENTIDADCLIENTE", -- VARCHAR(40)
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "NombrePortafolio"  AS "NOMBREPORTAFOLIO", -- VARCHAR(100)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdMonedaPrincipal"  AS "IDMONEDAPRINCIPAL", -- VARCHAR(3)
                  "NombreMonedaPrincipal"  AS "NOMBREMONEDAPRINCIPAL", -- VARCHAR(100)
                  "MontoMonedaPrincipal"  AS "MONTOMONEDAPRINCIPAL", -- DECIMAL
                  "IdMonedaSecundaria"  AS "IDMONEDASECUNDARIA", -- VARCHAR(3)
                  "NombreMonedaSecundaria"  AS "NOMBREMONEDASECUNDARIA", -- VARCHAR(100)
                  "MontoMonedaSecundaria"  AS "MONTOMONEDASECUNDARIA", -- DECIMAL
                  "TipoCambioCierre"  AS "TIPOCAMBIOCIERRE", -- DECIMAL
                  "ParidadCierre"  AS "PARIDADCIERRE", -- DECIMAL
                  "TipoCambioCosto"  AS "TIPOCAMBIOCOSTO", -- DECIMAL
                  "ParidadCosto"  AS "PARIDADCOSTO", -- DECIMAL
                  "MontoEquivalente"  AS "MONTOEQUIVALENTE", -- DECIMAL
                  "Margen"  AS "MARGEN", -- DECIMAL
                  "MontoDiferido"  AS "MONTODIFERIDO", -- DECIMAL
                  "PlazoResidual"  AS "PLAZORESIDUAL", -- INT
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "NombreInstrumento"  AS "NOMBREINSTRUMENTO", -- VARCHAR(100)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "NombreProducto"  AS "NOMBREPRODUCTO", -- VARCHAR(100)
                  "IdMedioPagoEntregamos"  AS "IDMEDIOPAGOENTREGAMOS", -- VARCHAR(3)
                  "NombreMedioPagoEntregamos"  AS "NOMBREMEDIOPAGOENTREGAMOS", -- VARCHAR(100)
                  "MontoPagoEntregamos"  AS "MONTOPAGOENTREGAMOS", -- DECIMAL
                  "FechaLiquidacionEntregamos"  AS "FECHALIQUIDACIONENTREGAMOS", -- VARCHAR(10)
                  "IdMedioPagoRecibimos"  AS "IDMEDIOPAGORECIBIMOS", -- VARCHAR(3)
                  "NombreMedioPagoRecibimos"  AS "NOMBREMEDIOPAGORECIBIMOS", -- VARCHAR(100)
                  "MontoPagoRecibimos"  AS "MONTOPAGORECIBIMOS", -- DECIMAL
                  "FechaLiquidacionRecibimos"  AS "FECHALIQUIDACIONRECIBIMOS", -- VARCHAR(10)
                  "IdPlaza"  AS "IDPLAZA", -- BIGINT
                  "NombrePlaza"  AS "NOMBREPLAZA", -- VARCHAR(100)
                  "IdFixing"  AS "IDFIXING", -- BIGINT
                  "NombreFixing"  AS "NOMBREFIXING", -- VARCHAR(255)
                  "FechaFixing"  AS "FECHAFIXING", -- VARCHAR(10)
                  "FechaAnticipo"  AS "FECHAANTICIPO", -- VARCHAR(10)
                  "TCForward"  AS "TCFORWARD", -- DECIMAL
                  "IdTipoCanalIngreso"  AS "IDTIPOCANALINGRESO", -- VARCHAR(3)
                  "NombreCanalIngreso"  AS "NOMBRECANALINGRESO", -- VARCHAR(100)
                  "IdAgente"  AS "IDAGENTE", -- BIGINT
                  "NombreAgente"  AS "NOMBREAGENTE", -- VARCHAR(201)
                  "NombreEmpresa"  AS "NOMBREEMPRESA", -- VARCHAR(100)
                  "IdUsuarioCreacion"  AS "IDUSUARIOCREACION", -- BIGINT
                  "CodigoUsuarioCreacion"  AS "CODIGOUSUARIOCREACION", -- VARCHAR(40)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
				  "FolioCartera" AS "FOLIOCARTERA"	--BIGINT
        from "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_MovimientosFWD' AND WST."Key" = "IdOperacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosFWD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
