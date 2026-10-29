SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATermino"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATermino" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdOperacion")  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdOperacion" AS "IDOPERACION",										--INT DEFAULT 0 NOT NULL,
				"FolioOperacion" AS "FOLIOOPERACION",								--INT DEFAULT 0 NOT NULL,
				"FolioCartera" AS "FOLIOCARTERA",									--INT DEFAULT 0,
				"FechaOperacion" AS "FECHAOPERACION",								--NVARCHAR(5000),
				"IdTipoProducto" AS "IDTIPOPRODUCTO",								--NVARCHAR(4) NOT NULL,
				"IdTipoOperacion" AS "IDTIPOOPERACION",								--NVARCHAR(6) NOT NULL,
				"NombreTipoOperacion" AS "NOMBRETIPOOPERACION", 					--NVARCHAR(10) NOT NULL,
				"Intermesa" AS "INTERMESA", 										--NVARCHAR(1) DEFAULT 'N' NOT NULL,
				"IdMesaOrigen" AS "IDMESAORIGEN", 									--INT DEFAULT 0 NOT NULL,
				"NombreMesaOrigen" AS "NOMBREMESAORIGEN", 							--NVARCHAR(30), 
				"IdMesaDestino" AS "IDMESADESTINO",  								--INT,
				"NombreMesaDestino" AS "NOMBREMESADESTINO",							--NVARCHAR(30),
				"IdSocioDeNegocio" AS "IDSOCIODENEGOCIO",							--INT NOT NULL, 
				"IdCliente" AS "IDCLIENTE",											--INT DEFAULT 0 NOT NULL,
				"NombreCompleto" AS "NOMBRECOMPLETO",								--NVARCHAR(302) NOT NULL,
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",						--NVARCHAR(40) NOT NULL,
				"IdClienteCorredor" AS "IDCLIENTECORREDOR",							--INT DEFAULT 0 NOT NULL,
				"IdPortafolio" AS "IDPORTAFOLIO",									--INT DEFAULT 0 NOT NULL, 
				"CodigoPropositoPortafolio" AS "CODIGOPROPOSITOPORTAFOLIO",			--NVARCHAR(621),
				"AfectaCustodia" AS "AFECTACUSTODIA",								--NVARCHAR(6) NOT NULL,
				"IdPortafolioCorredor" AS "IDPORTAFOLIOCORREDOR",					--INT DEFAULT 0 NOT NULL,
				"IdEmpresa" AS "IDEMPRESA",											--INT DEFAULT 0 NOT NULL,
				"MontoOperacionInicial" AS "MONTOOPERACIONINICIAL",					--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"MontoOperacionFinal" AS "MONTOOPERACIONFINAL",						--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"IdTipoReajuste" AS "IDTIPOREAJUSTE",								--NVARCHAR(3) NOT NULL,
				"Tasa" AS "TASA",													--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"IdMoneda" AS "IDMONEDA",											--NVARCHAR(3) NOT NULL,
				"MontoOperacionInicialMoneda" AS "MONTOOPERACIONINICIALMONEDA",		--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"MontoOperacionFinalMoneda" AS "MONTOOPERACIONFINALMONEDA",			--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"IdMonedaLiquidacion" AS "IDMONEDALIQUIDACION",						--NVARCHAR(3) NOT NULL,
				"PlazoEnDias" AS "PLAZOENDIAS",										--INT DEFAULT 0 NOT NULL,
				"FechaVencimiento" AS "FECHAVENCIMIENTO",							--NVARCHAR(2000),
				"FechaLiquidacion" AS "FECHALIQUIDACION",							--NVARCHAR(2000),
				"IdCondicionLiquidacion" AS "IDCONDICIONLIQUIDACION",				--NVARCHAR(2) NOT NULL,
				"NombreCondicionLiquidacion" AS "NOMBRECONDICIONLIQUIDACION",		--NVARCHAR(100),
				"FechaCondicionLiquidacion" AS "FECHACONDICIONLIQUIDACION",			--NVARCHAR(2000),
				"IdAgente" AS "IDAGENTE",											--INT,
				"NombreEjecutivo" AS "NOMBREEJECUTIVO",								--NVARCHAR(302),
				"IdEstado" AS "IDESTADO",											--NVARCHAR(3) NOT NULL,
				"NombreEstado" AS "NOMBREESTADO",									--NVARCHAR(100),
				"FechaCreacion" AS "FECHACREACION",									--NVARCHAR(2000),
				"IdUsuarioCreacion" AS "IDUSUARIOCREACION",							--BIGINT,
				"CodigoUsuario" AS "CODIGOUSUARIO",									--NVARCHAR(100),
				"IdTipoCanalIngreso" AS "IDTIPOCANALINGRESO",						--NVARCHAR(3) NOT NULL,
				"Glosa" AS "GLOSA",													--NVARCHAR(500) NOT NULL,
				"MontoOperacionInicialAnticipo" AS "MONTOOPERACIONINICIALANTICIPO", --DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"MontoOperacionFinalAnticipo" AS "MONTOOPERACIONFINALANTICIPO",		--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"TasaAnticipo" AS "TASAANTICIPO",									--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"MontoOperacionInicialMonedaAnticipo" AS "MONTOOPERACIONINICIALMONEDAANTICIPO",	--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"MontoOperacionFinalMonedaAnticipo" AS "MONTOOPERACIONFINALMONEDAANTICIPO",		--DECIMAL(26,8) DEFAULT 0 NOT NULL,
				"IdMonedaLiquidacionAnticipo" AS "IDMONEDALIQUIDACIONANTICIPO",					--NVARCHAR(3)
				"IdCondicionLiquidacionAnticipo" AS "IDCONDICIONLIQUIDACIONANTICIPO",			--NVARCHAR(2)
				"NombreCondicionLiquidacionAnticipo" AS "NOMBRECONDICIONLIQUIDACIONANTICIPO",	--NVARCHAR(100)
				"FechaCondicionLiquidacionAnticipo" AS "FECHACONDICIONLIQUIDACIONANTICIPO"		--NVARCHAR(2000)

        from "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperacionesATerminoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_OperacionesATermino' AND WST."Key" = "IdOperacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_OperacionesATermino" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
