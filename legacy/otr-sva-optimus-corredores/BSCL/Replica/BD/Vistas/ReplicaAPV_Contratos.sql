SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaAPV_Contratos"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaAPV_Contratos" AS

        Select WST."IdStack" AS "IDSTACK",
        		  "IdContratoSAAPV"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdContratoSAAPV"  AS "IDCONTRATOSAAPV", -- BIGINT
                  "NombrePlanAhorro"  AS "NOMBREPLANAHORRO", -- VARCHAR(100)
                  "FechaSuscripcion"  AS "FECHASUSCRIPCION", -- VARCHAR(10)
                  "FolioContrato"  AS "FOLIOCONTRATO", -- VARCHAR(50)
                  "DocumentoIdentidadCliente"  AS "DOCUMENTOIDENTIDADCLIENTE", -- VARCHAR(40)
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(100)
                  "ApellidoPaternoCliente"  AS "APELLIDOPATERNOCLIENTE", -- VARCHAR(100)
                  "ApellidoMaternoCliente"  AS "APELLIDOMATERNOCLIENTE", -- VARCHAR(100)
                  "IdEmpleadoEjecutivo"  AS "IDEMPLEADOEJECUTIVO", -- BIGINT
                  "NombrePersonaEjecutivo"  AS "NOMBREPERSONAEJECUTIVO", -- VARCHAR(300)
                  "FechaNacimientoCliente"  AS "FECHANACIMIENTOCLIENTE", -- VARCHAR(10)
                  "Nacionalidad"  AS "NACIONALIDAD", -- VARCHAR(100)
                  "EstadoCivil"  AS "ESTADOCIVIL", -- VARCHAR(100)
                  "Genero"  AS "GENERO", -- VARCHAR(9)
                  "CargoPersona"  AS "CARGOPERSONA", -- VARCHAR(100)
                  "CategoriaInversionista"  AS "CATEGORIAINVERSIONISTA", -- VARCHAR(40)
                  "PerfilComercial"  AS "PERFILCOMERCIAL", -- VARCHAR(40)
                  "EmailCliente"  AS "EMAILCLIENTE", -- VARCHAR(100)
                  "DocumentoIdentidadEmpleador"  AS "DOCUMENTOIDENTIDADEMPLEADOR", -- VARCHAR(40)
                  "NombreEncargado"  AS "NOMBREENCARGADO", -- VARCHAR(100)
                  "EmailEncargado"  AS "EMAILENCARGADO", -- VARCHAR(100)
                  "TelefonoEncargado"  AS "TELEFONOENCARGADO", -- VARCHAR(40)
                  "MedioRecaudacion"  AS "MEDIORECAUDACION", -- VARCHAR(10)
                  "EntidadRecaudadora"  AS "ENTIDADRECAUDADORA", -- VARCHAR(20)
                  "CodigoPrevision"  AS "CODIGOPREVISION", -- BIGINT
                  "Pensionado"  AS "PENSIONADO", -- VARCHAR(2)
                  "Trabajador"  AS "TRABAJADOR", -- VARCHAR(30)
                  "FormaDePago"  AS "FORMADEPAGO", -- VARCHAR(10)
                  "Recaudador"  AS "RECAUDADOR", -- VARCHAR(26)
                  "DiaDescuento"  AS "DIADESCUENTO", -- BIGINT
                  "Recaudacion"  AS "RECAUDACION", -- VARCHAR(10)
                  "Institucion"  AS "INSTITUCION", -- VARCHAR(24)
                  "ComisionTransferencia"  AS "COMISIONTRANSFERENCIA", -- DECIMAL
                  "NombreEntidadOrigen"  AS "NOMBREENTIDADORIGEN", -- VARCHAR(100)
                  "RutEntidadOrigen"  AS "RUTENTIDADORIGEN", -- VARCHAR(11)
                  "TipoDeAhorro"  AS "TIPODEAHORRO", -- VARCHAR(10)
                  "FondoDeOrigen"  AS "FONDODEORIGEN", -- VARCHAR(10)
                  "TipoTraspaso"  AS "TIPOTRASPASO", -- VARCHAR(20)
                  "MontoTraspaso"  AS "MONTOTRASPASO", -- DECIMAL
                  "UFTraspaso"  AS "UFTRASPASO", -- DECIMAL
                  "PorcentajeTraspaso"  AS "PORCENTAJETRASPASO", -- DECIMAL
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "Producto"  AS "PRODUCTO", -- VARCHAR(35)
                  "MontoPesos"  AS "MONTOPESOS", -- DECIMAL
                  "MontoUF"  AS "MONTOUF", -- DECIMAL
                  "Porcentaje"  AS "PORCENTAJE", -- DECIMAL
                  "Pago"  AS "PAGO", -- VARCHAR(20)
                  "PeriodoInicio"  AS "PERIODOINICIO", -- VARCHAR(7)
                  "PeriodoTermino"  AS "PERIODOTERMINO", -- VARCHAR(7)
                  "RegimenTributario"  AS "REGIMENTRIBUTARIO", -- VARCHAR(50)
                  "IdMatrizAgrupadorAPV"  AS "IDMATRIZAGRUPADORAPV", -- BIGINT
                  "CodigoAgrupador"  AS "CODIGOAGRUPADOR", -- VARCHAR(100)
                  "Proposito"  AS "PROPOSITO", -- VARCHAR(45)
                  "CodigoTipoSolicitud"  AS "CODIGOTIPOSOLICITUD", -- VARCHAR(3)
                  "TipoMovimiento"  AS "TIPOMOVIMIENTO", -- VARCHAR(10)
		  			"IdDetalleOrigenTraspaso"	as "IDDETALLEORIGENTRASPASO", --BIGINT
                  "DocumentoIdentidadEntidadDestino"  AS "DOCUMENTOIDENTIDADENTIDADDESTINO", -- VARCHAR(50)
                  "NombreEntidadDestino"  AS "NOMBREENTIDADDESTINO", -- VARCHAR(100)
                  "IdSerieFondo"  AS "IDSERIEFONDO", -- VARCHAR(50)
                  "IdFondo"  AS "IDFONDO", -- VARCHAR(100)
		  			"MontoDetalleTraspaso" AS "MONTODETALLETRASPASO", --DECIMAL
                  "FondoDestino"  AS "FONDODESTINO", -- VARCHAR(100)
                  "RegimenTributarioDestino"  AS "REGIMENTRIBUTARIODESTINO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "EstadoContrato"  AS "ESTADOCONTRATO", -- VARCHAR(100)
                  "EsADC"  AS "ESADC", -- VARCHAR(1)
		   "IdMedioRecaudacionFormaPago" AS "IDMEDIORECAUDACIONFORMAPAGO" --BIGINT
        from "BSCL.APV.BD.Vistas::ConsultarContratosAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'APV_Contratos' AND WST."Key" = "IdContratoSAAPV"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaAPV_Contratos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
