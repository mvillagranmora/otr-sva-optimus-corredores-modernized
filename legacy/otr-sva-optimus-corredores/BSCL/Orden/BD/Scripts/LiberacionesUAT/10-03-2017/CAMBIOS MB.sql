SET SCHEMA "BSCL";
ALTER TABLE "BSCL.Plataforma.BD::ACC.Usuario" ADD ("CodigoMenu" NVARCHAR  (100 )  not null  default ''  );


--AUTORIZACIONES

ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" ADD ("SPConfirma" NVARCHAR  (500 )  not null  default ''  );
ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" ADD ("SPCampoAutorizacion" NVARCHAR  (255 )  not null  default ''  );
ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" ADD ("SPCampoNegocio" NVARCHAR  (255 )  not null  default ''  );
ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" ADD ("SPRechaza" NVARCHAR  (500 )  not null  default ''  );
ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" ADD ("SPFormato" INTEGER  not null  default '10'  );
ALTER TABLE "BSCL.Plataforma.BD::AUT.TipoEvento" DROP ("SPConfirmar");

SELECT * FROM "BSCL.Plataforma.BD::AUT.TipoEvento" WHERE "IdTipoEvento" = 'INGORD';

update "BSCL.Plataforma.BD::AUT.TipoEvento" SET
    "SPConfirma"='BSCL.Orden.BD.Servicios::ConfirmarAutorizacionOrdenes',
    "SPRechaza" ='BSCL.Orden.BD.Servicios::RechazaAutorizacionOrdenes',
    "SPFormato"=4, --4 campos o 10 campos
    "SPCampoAutorizacion"='NumeroOperacionConcepto',
    "SPCampoNegocio"='IdOrden'
where "IdTipoEvento" = 'INGORD';

update "BSCL.Plataforma.BD::AUT.TipoEvento" SET
    "SPConfirma"='BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaTelefonoMasivo',
    "SPRechaza" ='BSCL.SocioNegocio.BD.Servicios::PER.RechazaAutorizacionPersonaTelefonoMasivo',
    "SPFormato"=10, --4 campos o 10 campos
    "SPCampoAutorizacion"='NumeroOperacionConcepto',
    "SPCampoNegocio"='IdTelefono'
where "IdTipoEvento" = 'MODTEL';

UPDATE BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
set "RutaServicio" = 'PLUGIN_PLA.jar@cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo' , "UsoServicio" = 0, "TipoServicio" = 'J'
WHERE "IdServicio" = 41;


alter table "BSCL.Orden.BD::ORD.LibroOrdenes" drop ("LibroPorDefecto");
alter table "BSCL.Orden.BD::ORD.LibroOrdenes" drop ("AsignacionAutomatica");
alter table "BSCL.Orden.BD::ORD.LibroOrdenes"  add ( "LibroPorDefecto"  NVARCHAR (1) NOT NULL DEFAULT 'N'   ) ;
alter table "BSCL.Orden.BD::ORD.LibroOrdenes"  add ( "AsignacionAutomatica"  NVARCHAR (1) NOT NULL DEFAULT 'N'   ) ;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3266,'BSCL.Custodia.BD.Vistas::CUS.ConsultarUsoFlujoCustodia',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3237,'BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenesGrupo',0,'Q','','','');


ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ALTER ("IdTipoAsignacion" NVARCHAR(40) NULL);

UPDATE "BSCL.Orden.BD::ORD.EstadoOrden" set "IdEstado" = 'ABT' WHERE "IdEstado" = 'ACT' AND "NombreEntidad" = 'LIBRO.ORDENES';
UPDATE "BSCL.Orden.BD::ORD.EstadoOrden" set "IdEstado" = 'CER' WHERE "IdEstado" = 'INA' AND "NombreEntidad" = 'LIBRO.ORDENES';

UPDATE "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'ABT' WHERE "IdEstado" = 'ACT';
UPDATE "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'CER' WHERE "IdEstado" = 'INA';

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador" ALTER ("CodigoTipoIndicador"   	NVARCHAR(40) 	NOT NULL)

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3251,'BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3252,'BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa',0,'P','','','');

delete from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 315



INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento"("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
SELECT 'OK' AS "IdTipoComportamiento",'OMITIR' AS "Nombre",1 AS "IdUsuarioCreador",CURRENT_TIMESTAMP AS "FechaCreacion",CURRENT_TIMESTAMP AS"FechaModificacion",1 AS "IdEmpresa",'ACT' as "IdEstado"
FROM DUMMY;


INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('DIFERENCIAPRECIOORDEN', 'PORCENTAJE DE DIFERENCIA PRECIO ORDEN Y PRECIO MERCADO', 'N', 'N', 'ADVERTENCIA', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(64, 'CORREDORA', 'DIFERENCIAPRECIOORDEN', 'INGRESOORDEN', 'ADVERTENCIA', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');


INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(65, 'CORREDORA', 'DIFERENCIAPRECIOORDEN', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(66, 'CORREDORA', 'DIFERENCIAPRECIOORDEN', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');





INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(67, 'CORREDORA', 'CLIENTEBLOQUEADO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(68, 'CORREDORA', 'PERSONABLOQUEADA', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(69, 'CORREDORA', 'OPERACIONSINFLUJOCUSTODIA', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(70, 'CORREDORA', 'EJECUTIVOSINPRODUCTO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(71, 'CORREDORA', 'EJECUTIVOPORTAFOLIOBLOQUEADO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(72, 'CORREDORA', 'MONTOOPERACIONSUPERAPERMITIDO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(73, 'CORREDORA', 'COMISIONSUPERAMAXIMOPERMITIDO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(74, 'CORREDORA', 'COMISIONBAJOMINIMOPERMITIDO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(75, 'CORREDORA', 'MONTOSUPERAMAXIMOCANALDISTRIBUCION', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(76, 'CORREDORA', 'ORDENREQUIEREAUTORIZACION', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(77, 'CORREDORA', 'DESCUENTOCOMISIONSUPERAPERMITIDO', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(78, 'CORREDORA', 'FOTOCOPIACARNET', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(79, 'CORREDORA', 'ANTECEDENTESLEGALES', 'INGRESOORDEN', 'OK', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');


CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.ClaseCobro" (
	"IdClaseCobro"			NVARCHAR(40) NOT NULL,
	"NombreClaseCobro"		NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdClaseCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.BaseTasaCobro" (
	"IdBaseTasaCobro"		BIGINT,
	"NombreBaseTasaCobro"	NVARCHAR(100) NOT NULL,
	"ValorBaseTasaCobro" 	DECIMAL(26,8) NOT NULL,
	PRIMARY KEY ("IdBaseTasaCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial" (
	"IdEstadoPlataformaComercial"	BIGINT,
	"IdEstado"						NVARCHAR(100) NOT NULL,
	"NombreEntidad" 				NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdEstadoPlataformaComercial")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.TipoCobro" (
	"IdTipoCobro"				NVARCHAR(40) NOT NULL,
	"NombreTipoCobro"			NVARCHAR(100) NOT NULL,
	"PermiteMontoFijo"			NVARCHAR(1) NOT NULL DEFAULT 'S',
	"PermitePorcentajeFijo"		NVARCHAR(1) NOT NULL DEFAULT 'S',
	"PermitePorcentajeRangos"	NVARCHAR(1) NOT NULL DEFAULT 'S',
	PRIMARY KEY ("IdTipoCobro")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.TipoRangoCobro" (
	"IdTipoRangoCobro"		NVARCHAR(40),
	"NombreTipoRangoCobro"	NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdTipoRangoCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" (
	"IdEmpresa"								BIGINT NOT NULL,
	"IdClaseCobro"							NVARCHAR(40) NOT NULL,
	"IdTipoCobro"							NVARCHAR(40) NOT NULL,
	"CodigoDefinicionCobro"					NVARCHAR(255) NOT NULL,
	"IdEstado"								NVARCHAR(3) NOT NULL,
	"Descripcion"							NVARCHAR(5000) NOT NULL DEFAULT '',
	
	"CheckCobroMonedaOperacion"				NVARCHAR(1) NOT NULL DEFAULT 'S',
	"IdMonedaCobroOtraMoneda"				NVARCHAR(3) NULL,
	
	--PARA MONTO FIJO
	"MontoFijo_MontoCobro"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoFijo_CheckMontoOperacionMenor"	NVARCHAR(1) NOT NULL DEFAULT 'N',
	"MontoFijo_ValorMontoOperacionMenor"	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoFijo_CheckSiNo"					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"MontoFijo_ValorMontoSiNo"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	
	--PARA PORCENTUAL FIJO
	"PorcentualFijo_ValorCobro"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_CheckSobreMonto"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckSobreCantidad"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMayorEntreDos"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMenorEntreDos"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMontoMinimo"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_ValorMontoMinimo"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_CheckMontoMaximo"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_ValorMontoMaximo"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_IdMonedaMinimoMaximo"	NVARCHAR(3) NULL,
	
	--OTROS DATOS
	"Otros_CheckOperacionContado"			NVARCHAR(1) NOT NULL DEFAULT 'S',
	"Otros_CheckOperacionPlazo"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckBaseTasa"					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_ValorBaseTasa"					BIGINT NULL,
	"Otros_CheckInteresSimple"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckInteresCompuesto"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	
	"Otros_CheckAllIn"						NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_ValorAllInMontoOperacionBase"	DECIMAL(26,8) NOT NULL DEFAULT 0,
	
	"Otros_CheckAfectoImpuesto"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckAplicaPortafolio"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckAplicaCorresponsal"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckPermiteAumentarValor"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckPermiteDisminuirValor"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	
	
	
	PRIMARY KEY ("IdEmpresa","CodigoDefinicionCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroEmisor" (
	"IdEmpresa"				BIGINT NOT NULL,
	"CodigoDefinicionCobro"	NVARCHAR(40) NOT NULL,
	"IdEmisor"				NVARCHAR(40) NOT NULL,
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdEmisor")
);

ALTER TABLE "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" ADD ("Alias" NVARCHAR(255) NOT NULL DEFAULT '');

ALTER TABLE "BSCL.Portafolio.BD::POR.Autorizado" add("NumeroRegistroPublico" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Portafolio.BD::POR.Autorizado" add("Asiento" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Portafolio.BD::POR.Autorizado" add("FichaPartida" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Portafolio.BD::POR.Autorizado" add("TipoFirma" NVARCHAR(40) NOT NULL DEFAULT '');

COMMIT;




