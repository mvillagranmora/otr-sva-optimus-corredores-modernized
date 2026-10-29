SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosSpot"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosSpot" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdOperacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdOperacion"  AS "IDOPERACION", -- BIGINT
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "IdMesa"  AS "IDMESA", -- BIGINT
                  "NombreMesa"  AS "NOMBREMESA", -- VARCHAR(30)
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(10)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "DocumentoIdentidadCliente"  AS "DOCUMENTOIDENTIDADCLIENTE", -- VARCHAR(40)
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "NombrePortafolio"  AS "NOMBREPORTAFOLIO", -- VARCHAR(100)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdMonedaPrincipal"  AS "IDMONEDAPRINCIPAL", -- VARCHAR(3)
                  "MontoMonedaPrincipal"  AS "MONTOMONEDAPRINCIPAL", -- DECIMAL
                  "IdMonedaSecundaria"  AS "IDMONEDASECUNDARIA", -- VARCHAR(3)
                  "MontoMonedaSecundaria"  AS "MONTOMONEDASECUNDARIA", -- DECIMAL
                  "ParidadCosto"  AS "PARIDADCOSTO", -- DECIMAL
                  "TipoCambioCosto"  AS "TIPOCAMBIOCOSTO", -- DECIMAL
                  "ParidadCierre"  AS "PARIDADCIERRE", -- DECIMAL
                  "TipoCambioCierre"  AS "TIPOCAMBIOCIERRE", -- DECIMAL
                  "Margen"  AS "MARGEN", -- DECIMAL
                  "OrigenForward"  AS "ORIGENFORWARD", -- VARCHAR(1)
                  "Producto"  AS "PRODUCTO", -- VARCHAR(4)
                  "NombreProducto"  AS "NOMBREPRODUCTO", -- VARCHAR(100)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "NombreInstrumento"  AS "NOMBREINSTRUMENTO", -- VARCHAR(100)
                  "IdMedioPagoEntregamos"  AS "IDMEDIOPAGOENTREGAMOS", -- VARCHAR(3)
                  "NombreMedioPagoEntregamos"  AS "NOMBREMEDIOPAGOENTREGAMOS", -- VARCHAR(100)
                  "MontoPagoEntregamos"  AS "MONTOPAGOENTREGAMOS", -- DECIMAL
                  "FechaLiquidacionEntregamos"  AS "FECHALIQUIDACIONENTREGAMOS", -- VARCHAR(10)
                  "IdMedioPagoRecibimos"  AS "IDMEDIOPAGORECIBIMOS", -- VARCHAR(3)
                  "NombreMedioPagoRecibimos"  AS "NOMBREMEDIOPAGORECIBIMOS", -- VARCHAR(100)
                  "MontoPagoRecibimos"  AS "MONTOPAGORECIBIMOS", -- DECIMAL
                  "FechaLiquidacionRecibimos"  AS "FECHALIQUIDACIONRECIBIMOS", -- VARCHAR(10)
                  "PuntaCompra"  AS "PUNTACOMPRA", -- DECIMAL
                  "PuntaVenta"  AS "PUNTAVENTA", -- DECIMAL
                  "CodigoComercio"  AS "CODIGOCOMERCIO", -- BIGINT
                  "NombreCodigoComercio"  AS "NOMBRECODIGOCOMERCIO", -- VARCHAR(255)
                  "IdComprobanteFacturacion"  AS "IDCOMPROBANTEFACTURACION", -- BIGINT
                  "OperacionFicticia"  AS "OPERACIONFICTICIA", -- VARCHAR(1)
                  "IdAgente"  AS "IDAGENTE", -- BIGINT
                  "NombreAgente"  AS "NOMBREAGENTE", -- VARCHAR(201)
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "CodigoUsuario"  AS "CODIGOUSUARIO", -- VARCHAR(40)
                  "IdCanalIngreso"  AS "IDCANALINGRESO", -- VARCHAR(3)
                  "NombreCanalIngreso"  AS "NOMBRECANALINGRESO", -- VARCHAR(100)
                  "NombreEmpresa"  AS "NOMBREEMPRESA", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdSolicitudAporteRescate" AS "IDSOLICITUDAPORTERESCATE", --BIGINT
		  "IdOperacionSpotOrigen" as "IDOPERACIONSPOTORIGEN",
                  "IdPlataformaNegociacion" as "IDPLATAFORMANEGOCIACION", --VARCHAR(4)
                  "NombrePlataformaNegociacion" AS "NOMBREPLATAFORMANEGOCIACION" --VARCHAR(100)
        from "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_MovimientosSpot' AND WST."Key" = "IdOperacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_MovimientosSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
