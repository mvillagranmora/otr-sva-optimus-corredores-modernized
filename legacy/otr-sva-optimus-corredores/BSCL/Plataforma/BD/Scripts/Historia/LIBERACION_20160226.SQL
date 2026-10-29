-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 26-02-2016
--
 
 
--################################################
--       JG.MODIFICACIONES MODELO PAIS/CUIDAD
--################################################
	SET SCHEMA "BSCL";

	ALTER TABLE "BSCL.Plataforma.BD::REG.Pais"
		ADD ("UsaDivisionPais" VARCHAR(1) not null default 'N');
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.DivisionPais" (
		 "IdDivisionPais" 		BIGINT CS_FIXED not null,
		 "IdPais"				BIGINT CS_FIXED	not null default 0,
		 "IdCiudad"				BIGINT CS_FIXED, -- permite null.
		 "NombreDivisionPais" 	NVARCHAR(100) 	not null default '',
		 "IdClasificacion1" 	BIGINT CS_FIXED not null default 0,
		 "IdClasificacion2" 	BIGINT CS_FIXED not null default 0,
		 PRIMARY KEY ("IdDivisionPais")
	 );

	 CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" (
		 "IdClasificacionDivisionPais" 		BIGINT CS_FIXED not null,
		 "NombreClasificacionDivisionPais" 	NVARCHAR(100) 	not null default '', --	LOS RIOS, VALDIVIA. 
		 "TipoClasificacionDivisionPais"	NVARCHAR(40)	not null default '', -- REGION	, PROVINCIA.
		 "IdPais"							BIGINT CS_FIXED	not null default 0,
		 PRIMARY KEY ("IdClasificacionDivisionPais")
	 );
	 
	  INSERT INTO "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais")
	  VALUES(1,'Los Ríos','Región',44);
	  
	  UPDATE "BSCL.Plataforma.BD::REG.Pais" SET "UsaDivisionPais" = 'S' WHERE "IdPais"=44;
	  
	ALTER TABLE "BSCL.Plataforma.BD::REG.Ciudad" 
	 	ADD ("IdPais" BIGINT CS_FIXED not null default 0); 
	 	
 	ALTER TABLE "BSCL.Plataforma.BD::REG.Ciudad" 
	 	DROP ("IdRegion"); 
	
	ALTER TABLE "BSCL.Plataforma.BD::REG.Region"
	   DROP CONSTRAINT "FK_Pais_Region";
	   
	UPDATE "BSCL.Plataforma.BD::REG.Ciudad" SET "IdPais" = 44; // los por defecto
	   
	ALTER TABLE "BSCL.Plataforma.BD::REG.Ciudad"
	   DROP CONSTRAINT "FK_Region_Ciudad";
	   
	DROP TABLE "BSCL.Plataforma.BD::REG.Region";
	 
--------------------------------------------------
--      FIN MODIFICACIONES MODELO PAIS/CUIDAD
--------------------------------------------------

--################################################
--       JG.CAMBIOS MODELO CODIGO BOLSA
--################################################
SET SCHEMA "BSCL";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.CorredoraBolsa" (
	 "IdCorredoraBolsa" 	BIGINT,
	 "IdBolsa" 				NVARCHAR(3)		NOT NULL DEFAULT 0,
	 "Codigo" 				NVARCHAR(40) 	NOT NULL DEFAULT '',
	 "CodigoSVS" 			NVARCHAR(40)	NOT NULL DEFAULT '',   -- MAPEA CON SOCIO NEGOCIO!
	 "IdEstado"				NVARCHAR(3) 	DEFAULT 'ACT',
	 "Borrado" 				NVARCHAR(1) 	DEFAULT 'N',
	 PRIMARY KEY ("IdCorredoraBolsa")
);
--------------------------------------------------
--      FIN CAMBIOS MODELO CODIGO BOLSA
--------------------------------------------------



--################################################
--       mmachmar - Inscripcion Accesos
--################################################

--Botones en pestaña Plantilla Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(205,'Editar Plantilla Comercial', 'BTN', 204, 1, 0, 'AdmPla_PlantillaComercial_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(206,'Crear Plantilla Comercial', 'BTN', 204, 2, 0, 'AdmPla_PlantillaComercial_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(207,'Guardar Plantilla Comercial', 'BTN', 204, 3, 46, 'AdmPla_PlantillaComercial_guardar', 'S');


--Pestaña Plantilla Productos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(208,'Pestaña Plantilla Productos', 'BTN', 203, 2, 0, 'AdmPla_PlantillaProductos', 'S');
--Botones en pestaña Plantilla Productos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(209,'Editar Plantilla Productos', 'BTN', 208, 1, 0, 'AdmPla_PlantillaProductos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(210,'Crear Plantilla Productos', 'BTN', 208, 2, 0, 'AdmPla_PlantillaProductos_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(211,'Guardar Plantilla Productos', 'BTN', 208, 3, 45, 'AdmPla_PlantillaProductos_guardar', 'S');


--Pestaña Plantilla Cobros
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(212,'Pestaña Plantilla Cobros', 'BTN', 203, 3, 0, 'AdmPla_PlantillaCobros', 'S');
--Botones en pestaña Plantilla Cobros
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(213,'Editar Plantilla Cobros', 'BTN', 212, 1, 0, 'AdmPla_PlantillaCobros_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(214,'Crear Plantilla Cobros', 'BTN', 212, 2, 0, 'AdmPla_PlantillaCobros_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(215,'Guardar Plantilla Cobros', 'BTN', 212, 3, 44, 'AdmPla_PlantillaCobros_guardar', 'S');


--Pestaña Plantilla Facturación
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(216,'Pestaña Plantilla Facturación', 'BTN', 203, 4, 0, 'AdmPla_PlantillaFacturacion', 'S');


--Pestaña Plantilla Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(217,'Pestaña Plantilla Condiciones Operativas', 'BTN', 203, 5, 0, 'AdmPla_PlantillaCondicionesOperativas', 'S');
--Botones en pestaña Plantilla Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(218,'Editar Plantilla Condiciones Operativas', 'BTN', 217, 1, 0, 'AdmPla_PlantillaCondicionesOperativas_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(219,'Crear Plantilla Condiciones Operativas', 'BTN', 217, 2, 0, 'AdmPla_PlantillaCondicionesOperativas_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(220,'Guardar Plantilla Condiciones Operativas', 'BTN', 217, 3, 136, 'AdmPla_PlantillaCondicionesOperativas_guardar', 'S');


--Pestaña Grupo de Activos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(221,'Pestaña Grupo de Activos', 'BTN', 203, 6, 0, 'AdmPla_GrupoDeActivos', 'S');
--Botones en pestaña Grupo de Activos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(222,'Editar Grupo de Activos', 'BTN', 221, 1, 0, 'AdmPla_GrupoDeActivos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(223,'Crear Grupo de Activos', 'BTN', 221, 2, 0, 'AdmPla_GrupoDeActivos_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(224,'Guardar Grupo de Activos', 'BTN', 221, 3, 97, 'AdmPla_GrupoDeActivos_guardar', 'S');


--Pestaña Plantilla Empleado
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(225,'Pestaña Plantilla Empleado', 'BTN', 203, 7, 0, 'AdmPla_PlantillaEmpleado', 'S');
--Botones en pestaña Plantilla Empleado
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(226,'Editar Plantilla Empleado', 'BTN', 225, 1, 0, 'AdmPla_PlantillaEmpleado_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(227,'Crear Plantilla Empleado', 'BTN', 225, 2, 0, 'AdmPla_PlantillaEmpleado_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(228,'Guardar Plantilla Empleado', 'BTN', 225, 3, 143, 'AdmPla_PlantillaEmpleado_guardar', 'S');


----------------------------------------------------------------------------------------------------
--------------------------- PLATAFORMA - Mantenedor Atributos ------------------------------------
----------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(229,'Mantenedor Atributos', 'MENU2', 13, 3, 0, 'MantenedorAtributos', 'S');
--Botones en pantalla Mantenedor Atributos - Detalle Atributos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(230,'Crear Detalle Atributo', 'BTN', 229, 1, 0, 'MantenedorAtributos_crearDetalle', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(231,'Modificar Detalle Atributo', 'BTN', 229, 2, 0, 'MantenedorAtributos_modificarDetalle', 'S');
--Pantalla Modal Detalle Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(232,'Botón Aceptar Crear Detalle Atributo', 'BTN', 230, 1, 134, 'ModalDetalleAtributo_aceptarCrear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(233,'Botón Aceptar Modificar Detalle Atributo', 'BTN', 231, 1, 134, 'ModalDetalleAtributo_aceptarModificar', 'S');
--Botones en pantalla Mantenedor Atributos - Tipo Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(234,'Crear Tipo Atributo', 'BTN', 229, 3, 0, 'MantenedorAtributos_crearTipoAtr', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(235,'Modificar Tipo Atributo', 'BTN', 229, 4, 0, 'MantenedorAtributos_modificarTipoAtr', 'S');
--Pantalla Modal Tipo Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(236,'Botón Aceptar Crear Tipo Atributo', 'BTN', 234, 1, 122, 'ModalTipoAtributo_aceptarCrear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(237,'Botón Aceptar Modificar Tipo Atributo', 'BTN', 235, 1, 122, 'ModalTipoAtributo_aceptarModificar', 'S');


----------------------------------------------------------------------------------------------------
--------------------------- PLATAFORMA - Mantenedor Tipo Relaciones ------------------------------------
----------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(238,'Mantenedor Tipo Relaciones', 'MENU2', 13, 4, 0, 'MantenedorTipoRelaciones', 'S');
--Botones en pantalla Mantenedor Tipo Relaciones
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(239,'Agregar Tipo Relación', 'BTN', 238, 1, 0, 'MantenedorTipoRelaciones_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(240,'Modificar Tipo Relación', 'BTN', 238, 2, 0, 'MantenedorTipoRelaciones_modificar', 'S');
--Botones en pantalla Modal Tipo Relacion
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(241,'Botón Aceptar Agregar Tipo Relación', 'BTN', 239, 1, 180, 'ModalTipoRelacion_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(242,'Botón Aceptar Modificar Tipo Relación', 'BTN', 240, 1, 181, 'ModalTipoRelacion_aceptarModificar', 'S');

--------------------------------------------------
--      FIN Inscripcion Accesos
--------------------------------------------------



--################################################
--   mmachmar - Proposito en Mantenedor General
--################################################

INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('CLIENT','Cliente','ACT');


INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('PROPOS','CLIENT','PROPOS','Propósito','"BSCL.Portafolio.BD::POR.PropositoPortafolio"','ACT');


INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(47,'PROPOS','UI_PROPOSITO_PORTAFOLIO','S','S','N', 1, 'bcs-input-texto', '', 'IdPropositoPortafolio', 'NVARCHAR','40','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(48,'PROPOS','UI_DESCRIPCION','S','S','S', 2, 'bcs-input-texto', '', 'Descripcion', 'NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(49,'PROPOS','UI_ID_ESTADO','S','S','S',3,'bcs-combobox-base','combobox-estado-rol','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(50,'PROPOS','UI_NOMBRE_ESTADO','S','N','N',3,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');


INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(26,'PROPOS','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(27,'PROPOS','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(28,'PROPOS','UI_ELIMINAR','S',3,'ELIMINAR',3);

--------------------------------------------------
--     FIN Proposito en Mantenedor General
--------------------------------------------------

---------------------------------------------------
-- jvillagran - Informe de Clearing
---------------------------------------------------
-- Días no habiles(sábado y domingo) para 2016
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(77,'Inhabil','0001-01-01',5,'Sábado',2016,1)

INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(78,'Inhabil','0001-01-01',6,'Domingo',2016,1)

INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(79,'Inhabil','0001-01-01',5,'Sábado',2017,1)

INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(90,'Inhabil','0001-01-01',6,'Domingo',2017,1)

-- Inscripción Servicio
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(193,'BSCL.Tesoreria.BD.Servicios::ConsultarClearing',0,'P','');
---------------------------------------------------
-- jvillagran - Fin Informe de Clearing
---------------------------------------------------


--Creacion secuencia usuarios.
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Usuario"	CASCADE;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Usuario"
       START     WITH 10
       INCREMENT BY 1
       MINVALUE  1;

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(195,'BSCL.Orden.BD.Servicios::CrearGrupo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(200,'BSCL.Orden.BD.Servicios::ORD.ActualizarGrupoPortafolio',0,'P',''); 


---------------------------------------------------
-- cespinsoa - chequeras
---------------------------------------------------

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(192,'BSCL.Tesoreria.BD.Servicios::EliminarConfiguracionChequera',0,'P','');

---------------------------------------------------
-- cespinsoa - Fin chequeras
---------------------------------------------------

--Mantenedor Grupos clientes - CHERNANDEZ
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(243,'Consultar Grupo Clientes', 'MENU2', 3, 10, 0, 'ConsultarGruposPortafolio', 'S');

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(244,'Pantalla Crear Grupo', 'BTN', 243, 1, 0, 'ConsultaGruposPortafolio_crearGrupo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(245,'Pantalla Modificar Grupo', 'BTN', 243, 2, 0, 'ConsultaGruposPortafolio_modificarGrupo', 'S');

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(246,'BTN Crear Grupo', 'BTN', 244, 1, 195, 'CrearGrupoPortafolio_crearGrupo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(247,'BTN GUARDAR', 'BTN', 245, 1, 200, 'ModificarGrupo_actualizarGrupo', 'S');


alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteEmpresa1");
alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteEmpresa2");
alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteEmpresa3");
alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteCliente1");
alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteCliente2");
alter table "BSCL.Plataforma.BD::DOC.TipoDocumento" drop ("NombreFirmanteCliente3");
  

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(201,'BSCL.Mercado.BD.Servicios::ObtenerParidadTipoCambio',0,'P','');   


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(190, 'BSCL.Plataforma.BD.Servicios::VAL.ValorizarRVMasivo', 0, 'P', '');


DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 157;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(157,'BSCL.Plataforma.BD.Servicios::VAL.ValorizarRV',0,'P','');