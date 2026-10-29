--
-- SCRIPT PARA CREAR / MODIFICAR TABLAS
--

SET SCHEMA BSCL;

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("TasaReporte" DECIMAL(26,8)  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoReporte" INTEGER  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("BaseReporte" INTEGER  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("EsConfirmada" NVARCHAR(1)  not null  default 'S' );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("TasaPromedioReporte" DECIMAL(26,8)  not null  default 0 );

DROP TABLE "BSCL.Orden.BD::ORD.OrdenAsociada" 

CREATE TABLE "BSCL.Orden.BD::ORD.OrdenAsociada" (
	"IdOrdenAsociada" BIGINT NOT NULL, --Secuencia de Asociacion	
	"IdOrdenN" BIGINT NOT NULL,	--caso reporte: Orden reportado 
	"IdOrdenM" BIGINT NOT NULL,	--caso reporte: Orden reportante
	"FechaIngresoRegistroCorta" DATE NOT NULL DEFAULT '0001-01-01',
	"FechaIngresoRegistro" TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
	"ModalidadCalculo" NVARCHAR(1)  not null  default 'M', --Modalidad calculo M=Monto  C=Cantidad
	"Monto" DECIMAL(26,8)  not null  default 0,  --Monto Asociada de la IdOrdenM
	"Cantidad" DECIMAL(26,8)  not null  default 0,  --Cantidad Asociada de la IdOrdenM
	"IdUsuario" BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY ("IdOrdenAsociada")
);


select * from "BSCL.Orden.BD.Vistas::ConsultarEstadoAsociacionOrden"


CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::OrdenAsociada" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;
		
--create unique index "UNX01_OrdenAsociada" on "BSCL.Orden.BD::ORD.OrdenAsociada"("IdOrdenAsociada");

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("BaseReporte" INTEGER  not null  default 0 );

--TEMPORALES
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("TasaReporte" DECIMAL(26,8)  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("PlazoReporte" INTEGER  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("BaseReporte" INTEGER  not null  default 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("EsConfirmada" NVARCHAR(1)  not null  default 'S' );

select *  from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"  where "TipoServicio" = 'J' order by "IdServicio"

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(20026,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearAsociaciones',1,'J','Crea asociaciones de Ordenes 1-N');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(20027,'PLUGIN_ORD.jar@cl.bcs.ordenes.AnularAsociaciones',1,'J','Anula asociaciones de Ordenes');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(20029,'PLUGIN_ORD.jar@cl.bcs.ordenes.ConfirmarOrdenes',1,'J','Confirma Ordenes Ingresadas');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(20030,'PLUGIN_ORD.jar@cl.bcs.ordenes.ModificarTasaPromedioOrdenes',1,'J','Modifica Tasa de Ordenes Ingresadas');

commit;

select * from "BSCL.Orden.BD::ORD.OrdenAsociada"
select * from "BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 311601

select *  from "BSCL.Plataforma.BD::PLA.UsoTablaSistema" 

  SELECT "IdModulo"
  FROM "BSCL.Plataforma.BD::PLA.Modulo"
  WHERE "IdModulo" = 'ORD';


SELECT CRE."TipoEstado" AS "IdEstadoModulo",CRE."Nombre" as "NombreModulo"
FROM "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE 
WHERE CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = 1313;

