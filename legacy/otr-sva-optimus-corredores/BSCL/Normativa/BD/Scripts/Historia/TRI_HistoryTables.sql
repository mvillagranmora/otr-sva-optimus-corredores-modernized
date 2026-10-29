----------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------- PRUEBAS ---------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------

DROP TABLE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY";
COMMIT;

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY"
 (
 "IdCliente"            BIGINT      NOT NULL,
 "Nombre"               VARCHAR(20) NOT NULL,
 "Ciudad"               VARCHAR(20) ,
 PRIMARY KEY ("IdCliente")
);

INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" VALUES (1001,'Christina','Berlin');
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" VALUES (1002,'Philip', 'London' );
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" VALUES (1003,'John', 'New York' );
COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" MERGE DELTA INDEX;

SELECT LAST_COMMIT_ID FROM M_TRANSACTIONS WHERE CONNECTION_ID = CURRENT_CONNECTION;

SELECT SCHEMA_NAME, TABLE_NAME, SESSION_TYPE, TABLE_TYPE FROM SYS.TABLES WHERE TABLE_NAME = 'BSCL.SocioNegocio.BD::CLI.ClienteHISTORY'
SELECT *, "$validfrom$", "$validto$" FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" ('REQUEST_FLAGS'='ALLROWS');

UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" SET "Ciudad" = 'Miami' WHERE "Nombre" = 'John';
COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" MERGE DELTA INDEX;

---- ULTIMO COMMIT
SELECT LAST_COMMIT_ID FROM M_TRANSACTIONS WHERE CONNECTION_ID = CURRENT_CONNECTION;

DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" WHERE "Nombre" = 'Christina';
COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" MERGE DELTA INDEX;

---- ULTIMO COMMIT
SELECT * FROM M_TRANSACTIONS WHERE CONNECTION_ID = CURRENT_CONNECTION;
SELECT LAST_COMMIT_ID,* FROM M_HISTORY_INDEX_LAST_COMMIT_ID WHERE SESSION_ID = CURRENT_CONNECTION;

SELECT *, "$validfrom$", "$validto$" FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" ('REQUEST_FLAGS'='ALLROWS');
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" AS OF COMMIT ID 35261985;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" AS OF COMMIT ID 35263130;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" AS OF COMMIT ID 35263680;


------------------- Consultas por Fecha
SELECT COMMIT_TIME,* FROM SYS.TRANSACTION_HISTORY WHERE COMMIT_ID = 35263680;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" AS OF UTCTIMESTAMP '2016-02-02 14:04:21.48';

--Similar al método anterior, la sesión de la base de datos se puede configurar de nuevo a un cierto punto en el tiempo, en función del valor previsto UTC marca de tiempo.
SET HISTORY SESSIONTO UTCTIMESTAMP ‘2014-02-02 04:11:27.73’;

DELETE HISTORY FROM "<SCHEMA_NAME>"."TEST_HISTORY_TABLE";

 --Eliminar físicamente
 ALTER SYSTEM RECLAIM VERSION SPACE;
 MERGE HISTORY DELTA of "<SCHEMA_NAME>"."TEST_HISTORY_TABLE";

SELECT *, "$validfrom$", "$validto$" FROM "<SCHEMA_NAME>"."TEST_HISTORY_TABLE" ('REQUEST_FLAGS'= ('ALLCOMMITTED','HISTORYONLY'));

--- se puede fijar la fecha máxima que requiero operar, en este caso, elimina toda la historia menor o igual a '2013-07-00 00:00:00.000'
SET HISTORY SESSION TO UTCTIMESTAMP '2013-07-00 00:00:00.000';
DELETE HISTORY FROM "<SCHEMA_NAME>"."TEST_HISTORY_TABLE" ;

SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" AS OF UTCTIMESTAMP '2016-02-02 14:04:21.48';
SELECT * FROM SYS.TRANSACTION_HISTORY

----------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------- QUERY -----------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------
--- toma IDCommit de la sesion

SELECT "$validfrom$", "$validto$" ,*
FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" ('REQUEST_FLAGS'='ALLROWS') as cli
        inner join SYS.TRANSACTION_HISTORY HIS on (HIS."COMMIT_ID" =cli."$validfrom$")
WHERE cli."IdCliente" = 1003
and TO_DATE(HIS."COMMIT_TIME") = '2016-02-02'

----------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------- crear tabla y cargar datos --------------------------------------
----------------------------------------------------------------------------------------------------------------------------
drop TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" (
 "IdPersona"                BIGINT CS_FIXED NOT NULL ,
 "IdEstado"                 NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdDocumentoIdentidad"     BIGINT CS_FIXED,
 "Tipo"                     NVARCHAR(2) DEFAULT 'N' NOT NULL ,
 "Alias"                    NVARCHAR(100) DEFAULT '' NOT NULL ,
 "Nombres"                  NVARCHAR(100) DEFAULT '' NOT NULL ,
 "ApellidoPaterno"          NVARCHAR(100) DEFAULT '' NOT NULL ,
 "ApellidoMaterno"          NVARCHAR(100) DEFAULT '' NOT NULL ,
 "RazonSocial"              NVARCHAR(100) DEFAULT '' NOT NULL ,
 "IdGiroPrincipal"          BIGINT CS_FIXED,--*
 "IdPaisNacimiento"         BIGINT CS_FIXED,
 "IdPaisResidencia"         BIGINT CS_FIXED,
 "FechaNacimiento"          DAYDATE CS_DAYDATE DEFAULT '0001-01-01',
 "FechaDefuncion"           DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "Sexo"                     NVARCHAR(2) NOT NULL ,
 "EsExtranjero"             NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EstadoCivil"              NVARCHAR(2),
 "RegimenConyugal"          NVARCHAR(2),
 "Cargo"                    NVARCHAR(100) DEFAULT '' NOT NULL ,--*
 "Empleador"                NVARCHAR(100) DEFAULT '' NOT NULL ,--*
 "IdDireccionFisicaPrincipal"       BIGINT CS_FIXED,--*
 "IdDireccionElectronicaPrincipal"  BIGINT CS_FIXED,--*
 "IdTelefonoPrincipal"      BIGINT CS_FIXED,--*
 "IdProfesion"              BIGINT CS_FIXED,
 --"FotoPersona"                BLOB MEMORY THRESHOLD 1000,
-- "FotoFirmaPersona"       BLOB MEMORY THRESHOLD 1000,
 "FechaInicioActividad"     SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaIngreso"             SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"            SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"        SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "EsEnrolado"               NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "IdCarga"                  BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen"                 BIGINT CS_FIXED DEFAULT 0,
 "Borrado"                  NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdPersona"))  ;

drop table "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" (
 "IdCliente"                BIGINT      NOT NULL ,
 "IdPersona"                BIGINT      NOT NULL,
 "IdEmpresa"                BIGINT      NOT NULL,
 "IdEstado"                 NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdSegmentoCliente"        BIGINT,
 "IdCategoriaInversionista" NVARCHAR(40),
 "IdTipoBloqueo"            NVARCHAR(40),
 "FechaBloqueo"             SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo"        NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdUsuarioCreador"         BIGINT,
 "FechaIngreso"             SECONDDATE  NOT NULL ,
 "FechaCreacion"            SECONDDATE  NOT NULL ,
 "FechaModificacion"        SECONDDATE  NOT NULL ,
 "EsEnrolado"               NVARCHAR(1) DEFAULT 'N' NOT NULL,
 "IdPortafolioDefecto"      BIGINT      DEFAULT 0   NOT NULL,
 "IdCarga"                  BIGINT      DEFAULT 0,
 "Borrado"                  NVARCHAR(1) DEFAULT 'N',
 PRIMARY KEY ("IdCliente")) ;

drop table "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY" (
 "IdSocioDeNegocio" BIGINT  NOT NULL,
 "IdPersona"        BIGINT  NOT NULL,
 "IdEmpresa"        BIGINT  NOT NULL,
 "IdCliente"        BIGINT ,
 "IdEmpleado"       BIGINT ,
 "IdOperador"       BIGINT ,
 "IdProveedor"      BIGINT ,
 "IdContraparte"    BIGINT ,
 "Borrado"          NVARCHAR(1) DEFAULT 'N',
 PRIMARY KEY ("IdSocioDeNegocio"))  ;

drop table "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY" (
 "IdDireccionFisica"            BIGINT CS_FIXED NOT NULL ,
 "Calle"                        NVARCHAR(100) DEFAULT '' NOT NULL ,
 "Numero"                       NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Departamento"                 NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Sector"                       NVARCHAR(100) DEFAULT '' NOT NULL ,
 "CodigoPostal"                 NVARCHAR(100) DEFAULT '' NOT NULL ,
 "IdCiudad"                     BIGINT CS_FIXED,
 "CodigoProposito"              NVARCHAR(40),
 "Comentario"                   NVARCHAR(100),
 "IdEstado"                     NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso"                 SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"                SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"            SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdPersona"                    BIGINT CS_FIXED NOT NULL ,
 "IdEstadoAutorizacion"         NVARCHAR(3) DEFAULT '' NOT NULL ,
 "IdCarga"                      BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen"                     BIGINT CS_FIXED DEFAULT 0,
 "Borrado"                      NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdDireccionFisica"))  ;

drop table "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" (
 "IdDocumentoIdentidad"         BIGINT CS_FIXED NOT NULL ,
 "IdPersona"                    BIGINT CS_FIXED NOT NULL ,--*
 "IdTipoDocumentoIdentidad"     NVARCHAR(1) DEFAULT '0' NOT NULL ,
 "DocumentoIdentidad"           NVARCHAR(40) DEFAULT '' NOT NULL ,
 "NumeroSerie"                  NVARCHAR(40) DEFAULT '' NOT NULL ,
 "FechaEmision"                 DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "FechaVencimiento"             DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "IdEstado"                     NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso"                 SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"                SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"            SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdCarga"                      BIGINT CS_FIXED DEFAULT 0,
 "IdEstadoAutorizacion"         NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdOrigen"                     BIGINT CS_FIXED DEFAULT 0,
 "Borrado"                      NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdDocumentoIdentidad"))  ;

 drop table "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY" (
 "IdTelefono"                   BIGINT CS_FIXED NOT NULL ,
 "IdPais"                       BIGINT CS_FIXED,
 "Numero"                       NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Anexo"                        NVARCHAR(40) DEFAULT '' NOT NULL ,
 "CodigoProposito"              NVARCHAR(40),
-- "CodigoTipoMedioContacto" NVARCHAR(40),
 "Comentario"                   NVARCHAR(100),
 "IdEstado"                     NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso"                 SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"                SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"            SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdPersona"                    BIGINT CS_FIXED NOT NULL ,--*
 "IdEstadoAutorizacion"         NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdCarga"                      BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen"                     BIGINT CS_FIXED DEFAULT 0,
 "Borrado"                      NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdTelefono"))  ;

drop table "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY";

CREATE HISTORY COLUMN TABLE "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY" (
 "IdDireccionElectronica"        BIGINT CS_FIXED NOT NULL ,
 "Direccion"                     NVARCHAR(100) DEFAULT '' NOT NULL ,
 "CodigoProposito"               NVARCHAR(40),
 --"CodigoTipoMedioContacto"    NVARCHAR(40),
 "Comentario"                   NVARCHAR(100),
 "IdEstado"                     NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso"                 SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"                SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"            SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdPersona"                    BIGINT CS_FIXED NOT NULL ,--*
 "IdEstadoAutorizacion"         NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdCarga"                      BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen"                     BIGINT CS_FIXED DEFAULT 0,
 "Borrado"                      NVARCHAR(1) DEFAULT 'N',
  PRIMARY KEY ("IdDireccionElectronica"))  ;

drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocioHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::ClienteHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona_HISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidadHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::TelefonoHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionElectronicaHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionFisicaHISTORY";
drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::TarjetaBancariaHISTORY";

create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocioHISTORY" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::ClienteHISTORY" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona_HISTORY" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidadHISTORY";
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::TelefonoHISTORY";
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionElectronicaHISTORY";
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionFisicaHISTORY";
create sequence  "BSCL"."BSCL.SocioNegocio.BD.Secuencias::TarjetaBancariaHISTORY";

insert into "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" (
                    "IdPersona"
                    ,"IdEstado"
                    ,"IdDocumentoIdentidad"
                    ,"Tipo"
                    ,"Nombres"
                    ,"ApellidoPaterno"
                    ,"ApellidoMaterno"
                    ,"RazonSocial"
                    ,"IdGiroPrincipal"
                    ,"IdPaisNacimiento"
                    ,"IdPaisResidencia"
                    ,"FechaNacimiento"
                    ,"FechaDefuncion"
                    ,"Sexo"
                    ,"EsExtranjero"
                    ,"EstadoCivil"
                    ,"RegimenConyugal"
                    ,"Cargo"
                    ,"Empleador"
                    ,"IdDireccionFisicaPrincipal"
                    ,"IdDireccionElectronicaPrincipal"
                    ,"IdTelefonoPrincipal"
                    ,"IdProfesion"
                    ,"FechaInicioActividad"
                    ,"FechaIngreso"
                    ,"FechaCreacion"
                    ,"FechaModificacion"
                    ,"EsEnrolado"
                    ,"IdCarga"
                    ,"Alias"
                    ,"IdOrigen"
                )

                select
                    "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona_HISTORY".NEXTVAL as IdPersona --key    IdPersona :Generar con SECUENCIA
                    ,
                    'ACT'
                    ,0                          as IdDocumentoIdentidad --Documento."IdDocumentoIdentidad"
                    ,TipoPersona."ValorDestino" as Tipo
                    ,PERSONA."per_nombres"      as Nombres
                    ,PERSONA."per_paterno"      as ApellidoPaterno
                    ,PERSONA."per_materno"      as ApellidoMaterno
                    ,PERSONA."per_raz_social"   as RazonSocial
                    ,0                          as IdGiroPrincipal
                    ,PaisNacimiento."IdPais"    as IdPaisNacimiento
                    ,PaisResidencia."IdPais"    as IdPaisResidencia--, PERSONA."per_pais_iso"
                    ,PERSONA."per_fec_naci"     as FechaNacimiento
                    ,'0001-01-01'               as FechaDefuncion
                    ,PERSONA."per_sexo"         as Sexo
                    ,CASE when
                         PERSONA."per_sexo" <> 'CL'
                         then 'S'
                         else 'N'
                    END                         as EsExtranjero
                    ,ifnull(EstadoCivil."ValorDestino",'SO')    as EstadoCivil
                    ,ifnull(RegimenConyugal."ValorDestino",'SC') as RegimenConyugal
                    ,''
                    ,''
                    ,0                          as IdDireccionFisicaPrincipal
                    ,0                          as IdDireccionElectronicaPrincipal
                    ,0                          as IdTelefonoPrincipal
                    ,11                         as IdProfesion

                    ,PERSONA."per_fec_ingreso"  as FechaInicioActividad
                    ,PERSONA."per_fec_ingreso"  as FechaIngreso
                    ,PERSONA."per_fec_ingreso"  as FechaCreacion
                    ,PERSONA."per_fec_modifica" as FechaModificacion
                    ,'S'
                    , 0                         as IdCarga
                    ,''                         as Alias
                    , PERSONA."per_id"          as "IdOrigen"

                from "BSCL_VIRTUAL"."GES_PERSONA" PERSONA

                    left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoPersona
                      on  (TipoPersona."TipoValor" = 'TipoPersona' and
                           TipoPersona."Entidad" = 'CLIENTE' and
                           TipoPersona."ValorOrigen" = PERSONA."per_tip_per"
                          )

                    left outer join "BSCL"."BSCL.Plataforma.BD::REG.Pais" as PaisResidencia
                      on  (PaisResidencia."CodigoPais"= PERSONA."per_pais_iso")

                    left outer join "BSCL"."BSCL.Plataforma.BD::REG.Pais" as PaisNacimiento
                      on  (PaisNacimiento."CodigoPais"= PERSONA."nac_id")

                    left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as EstadoCivil
                      on  (EstadoCivil."TipoValor" = 'EstadoCivil' and
                           EstadoCivil."Entidad" = 'CLIENTE' and
                           EstadoCivil."ValorOrigen" = PERSONA."par_est_civil"
                          )

                    left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as RegimenConyugal
                      on  (RegimenConyugal."TipoValor" = 'RegimenConyugal' and
                           RegimenConyugal."Entidad" = 'CLIENTE' and
                           RegimenConyugal."ValorOrigen" = PERSONA."par_reg_conyugal"
                          );

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" MERGE DELTA INDEX;


insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY"(
                    "IdSocioDeNegocio",
                    "IdPersona",
                    "IdEmpresa"
                )
        select
            "BSCL"."BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocioHISTORY".NEXTVAL as IdSocioDeNegocio --SECUENCIA
            ,Persona."IdPersona"        as IdPersona
            ,1                  as IdEmpresa

            from  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" as Persona;

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY" MERGE DELTA INDEX;

insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY"(
             "IdDireccionFisica"
             ,"Calle"
             ,"Numero"
             ,"Departamento"
             ,"Sector"
             ,"CodigoPostal"
             ,"IdCiudad"
             ,"CodigoProposito"
             --"CodigoTipoMedioContacto" NVARCHAR(40),
             ,"Comentario"
             ,"IdEstado"
             --"FechaIngreso" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
             --"FechaCreacion" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
             --"FechaModificacion" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
             ,"IdPersona"
             ,"IdEstadoAutorizacion"
             ,"IdCarga"
             ,"IdOrigen")

            select
                "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionFisicaHISTORY".NEXTVAL as "IdDireccionFisica" -- Generar  Secuencia
                ,DIRECCION."dir_nombre"         as "Calle"
                ,DIRECCION."dir_numero"         as "Numero"
                ,DIRECCION."dir_complemento"    as "Departamento"
                ,''                             as "Sector"
                ,DIRECCION."dir_cod_postal"     as "CodigoPostal"
                ,0                              as "IdCiudad"
                ,''                             as "CodigoProposito"
                ,''                             as "Comentario"
                ,'ACT'                          as "IdEstado"
                -- "FechaIngreso" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                -- "FechaCreacion" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                -- "FechaModificacion" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                ,PER."IdPersona"                as "IdPersona" -- aun no tengo ID
                ,'ING'                          as "IdEstadoAutorizacion"
                ,0                      as "IdCarga"
                ,DIRECCION."per_id"             as "IdOrigen"
            from "BSCL_VIRTUAL"."GES_DIRECCION" DIRECCION
                    -- de personas creadas EN HANA
                    inner join  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" PER on (DIRECCION."per_id" = PER."IdOrigen") --and PER."IdCarga" = :p_IdCarga
            --- Crear direcciones que no existan como direccion fisica
            where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY" DIR
                                where DIRECCION."per_id" = DIR."IdOrigen"
                                and DIRECCION."dir_nombre" = DIR."Calle" -- criterio incorporado para permitir agregar direcciones a una misma persona
                             )
            and left(DIRECCION."par_tdi_id",6)='TDIREF' ;

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY" MERGE DELTA INDEX;

            insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY"(
            "IdDireccionElectronica"        ,
            "Direccion"                     ,
            "CodigoProposito"               ,
             --"CodigoTipoMedioContacto"    NVARCHAR(40),
            "Comentario"                    ,
            "IdEstado"                      ,
            "IdPersona"                     ,
            "IdEstadoAutorizacion"          ,
            "IdCarga"                       ,
            "IdOrigen"
            )

            select
                "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionElectronicaHISTORY".NEXTVAL as "IdDireccionElectronica" -- Generar  Secuencia
                ,DIRECCION."dir_nombre"         as "Direccion"
                ,''                             as "CodigoProposito"
                ,''                             as "Comentario"
                ,'ACT'                          as "IdEstado"
                -- "FechaIngreso"               SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                -- "FechaCreacion"              SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                -- "FechaModificacion"          SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                ,PER."IdPersona"                as "IdPersona" -- aun no tengo ID
                ,'ING'                          as "IdEstadoAutorizacion"
                ,0                      as "IdCarga"
                ,DIRECCION."per_id"             as "IdOrigen"
            from "BSCL_VIRTUAL"."GES_DIRECCION" DIRECCION
                    -- de personas creadas EN HANA
                    inner join  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" PER on (DIRECCION."per_id" = PER."IdOrigen") --and PER."IdCarga" = :p_IdCarga
            --- Crear direcciones que no existan como direccion electronica
            where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY" DIR
                                where DIRECCION."per_id" = DIR."IdOrigen"
                                and DIRECCION."dir_nombre" = DIR."Direccion" -- criterio incorporado para permitir agregar direcciones a una misma persona
                             )
            and left(DIRECCION."par_tdi_id",6)='TDIREE';

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY" MERGE DELTA INDEX;


            insert into "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY" (
             "IdTelefono"
             ,"IdPais"
             ,"Numero"
             ,"Anexo"
             ,"CodigoProposito"
                -- "CodigoTipoMedioContacto" NVARCHAR(40),
             ,"Comentario"
             ,"IdEstado"
             --,"FechaIngreso"
             --,"FechaCreacion"
             --,"FechaModificacion"
             ,"IdPersona"
             ,"IdEstadoAutorizacion"
             ,"IdCarga"
             ,"IdOrigen"
            )
            select
                "BSCL"."BSCL.SocioNegocio.BD.Secuencias::TelefonoHISTORY".NEXTVAL as "IdTelefono" -- Generar  Secuencia
                , 0                             as "IdPais"
                , TELEFONO."tel_numero"         as "Numero"
                , ''                            as "Anexo"
                , TELEFONO."par_tte_id"         as "CodigoProposito"
                , TELEFONO."tel_observacion"    as "Comentario"
                ,'ACT'                          as "IdEstado"
                --,"FechaIngreso"               SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                --,"FechaCreacion"              SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                --,"FechaModificacion"          SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
                ,PER."IdPersona"                as "IdPersona"
                ,'ING'                          as "IdEstadoAutorizacion"
                ,0  as "IdCarga"
                ,TELEFONO."per_id"              as "IdOrigen" -- criterio incorporado para permitir agregar telefonos a una misma persona
            from "BSCL_VIRTUAL"."GES_TELEFONO" TELEFONO
                    -- de personas creadas EN HANA
                    inner join  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" PER on (TELEFONO."per_id" = PER."IdOrigen") --and PER."IdCarga" = :p_IdCarga
            --- Crear direcciones que no existan
            where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY" FONO
                               where TELEFONO."per_id"   = FONO."IdOrigen"
                               and TELEFONO."tel_numero" = FONO."Numero"
                             );

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY" MERGE DELTA INDEX;

                insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY"(
                    "IdDocumentoIdentidad"
                    ,"IdPersona"
                    ,"DocumentoIdentidad"
                    ,"IdTipoDocumentoIdentidad"
                    ,"NumeroSerie"
                    ,"FechaEmision"
                    ,"FechaVencimiento"
                    ,"IdEstado"
                    ,"FechaIngreso"
                    ,"FechaCreacion"
                    ,"FechaModificacion"
                    ,"IdCarga"
                    ,"IdEstadoAutorizacion"
                    ,"IdOrigen"
                )
                select
                    "BSCL"."BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidadHISTORY".NEXTVAL as "IdDocumentoIdentidad" -- Generar IdDocumentoIdentidad Secuencia
                    ,PER."IdPersona"                                    as "IdPersona"
                    ,PERSONA."per_num_doc"                              as "DocumentoIdentidad"
                    ,ifnull(TipoDocumentoIdentidad."ValorDestino",'R')  as "IdTipoDocumentoIdentidad"
                    ,'' AS NumeroSerie
                    ,TO_DATE(PERSONA."per_fec_naci") as FechaEmision
                    ,'0001-01-01' as FechaVencimiento
                    ,'ACT' AS IdEstado
                    ,'0001-01-01 00:00:00' AS FechaIngreso
                    ,'0001-01-01 00:00:00' AS FechaCreacion
                    ,'0001-01-01 00:00:00' AS FechaModificacion
                    ,0 as IdCarga
                    ,'ING' as "IdEstadoAutorizacion"
                    ,PERSONA."per_id"

                from "BSCL_VIRTUAL"."GES_PERSONA" PERSONA
                    -- De personas creadas en la presente carga
                    inner join  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" PER on (PERSONA."per_id" = PER."IdOrigen")

                left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoDocumentoIdentidad
                    on  (TipoDocumentoIdentidad."TipoValor" = 'TipoDocumentoIdentidad' and
                         TipoDocumentoIdentidad."Entidad" = 'CLIENTE' and
                         TipoDocumentoIdentidad."ValorOrigen" = PERSONA."per_tip_doc"
                        )
                --- Crear documentos identidad que no existan
                where not exists (  select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" DOC
                                    where ltrim(rtrim(PERSONA."per_num_doc")) = ltrim(rtrim(DOC."DocumentoIdentidad"))
                                    and PERSONA."per_id"   = DOC."IdOrigen"
                                 );

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" MERGE DELTA INDEX;


            UPDATE  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"
            SET     "IdDireccionFisicaPrincipal" = IFNULL((select max(DIR."IdDireccionFisica")
                                                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY" DIR
                                                    where DIR."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"."IdPersona"
                                                    ),0);


            UPDATE  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"
            SET     "IdDireccionElectronicaPrincipal" = IFNULL((select max(DIR."IdDireccionElectronica")
                                                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY" DIR
                                                    where DIR."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"."IdPersona"
                                                    ),0);


            UPDATE  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"
            SET     "IdTelefonoPrincipal" = IFNULL((select max(TEL."IdTelefono")
                                                    from "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY" TEL
                                                    where TEL."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"."IdPersona"
                                                    ),0);

            UPDATE  "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"
            SET     "IdDocumentoIdentidad" = IFNULL((select  max(DOC."IdDocumentoIdentidad")
                                                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" as  DOC
                                                    where  DOC."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"."IdPersona"
                                                    and DOC."IdTipoDocumentoIdentidad" = 'R'
                                                    ),0);

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"  MERGE DELTA INDEX;


            insert into "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY"(
                    "IdCliente",
                    "IdPersona",
                    "IdEmpresa",
                    "IdEstado",
                    "IdSegmentoCliente",
                    "IdCategoriaInversionista",--????
                    --"IdTipoBloqueo",
                    "FechaBloqueo",
                    "ComentarioBloqueo",
                    "FechaIngreso",
                    "FechaCreacion",
                    "FechaModificacion",
                    "EsEnrolado",
                    "IdCarga",
                    "IdUsuarioCreador"
                )
            select
            "BSCL"."BSCL.SocioNegocio.BD.Secuencias::ClienteHISTORY".NEXTVAL as IdCliente   --key IdCliente         SECUENCIA
            ,PersonaH."IdPersona"           as IdPersona
            ,1                  as IdEmpresa
            , EstadoCliente."ValorDestino"  as "IdEstado"
            ,1                              as IdSegmentoCliente
            ,'CALIFICADO'                   as IdCategoriaInversionista
            --,''                           as IdTipoBloqueo
            ,PERSONA."per_fec_bloqueo"      as FechaBloqueo
            ,''                             as ComentarioBloqueo
            ,CLIENTE."cli_fec_ingreso"      as FechaIngreso
            ,CLIENTE."cli_fec_ingreso"      as FechaCreacion
            ,CLIENTE."cli_fec_modifica"     as FechaModificacion
            ,'S'                            as EsEnrolado
            ,0                  as IdCarga
            ,0                              as IdUsuarioCreador

            from "BSCL_VIRTUAL"."GES_PERSONA" PERSONA inner join "BSCL_VIRTUAL"."GES_CLIENTE" CLIENTE on (PERSONA."per_id" = CLIENTE."per_id")

            --- CLIENTE SEBRA debe tener creada la PERSONA en HANA
            inner join "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" as PersonaH on (PERSONA."per_id" = PersonaH."IdOrigen")
            LEFT OUTER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" as ClienteH ON (PersonaH."IdPersona" = ClienteH."IdPersona")

            ---- HOMOLOGA ESTADOS
            left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as EstadoCliente
                      on  (EstadoCliente."TipoValor" = 'EstadoCliente' and
                           EstadoCliente."Entidad" = 'CLIENTE' and
                           EstadoCliente."ValorOrigen" = CLIENTE."est_id"
                          )
            --- PERSONA HANA no debe tener creado un cliente para crearlo
            where IFNULL(ClienteH."IdCliente",0) = 0;

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY"  MERGE DELTA INDEX;

        -- 3) Marcar Clientes nuevos en Socio de Negocio
            UPDATE  "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY"
            SET     "IdCliente" = ( select max(CLI."IdCliente")  --  grabar ID Cliente en Socio de NEgocio
                        from "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" PER, "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" CLI
                        where PER."IdPersona" = CLI."IdPersona" --- persona tenga un cliente creado
                        and   PER."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY"."IdPersona" -- persona creada como socio de negocio
                      );

COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocioHISTORY"  MERGE DELTA INDEX;

----------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------- CREAR VISTA -----------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------
DROP VIEW "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente";

CREATE VIEW "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente"(
 "COMMIT_TIME"
,"COMMIT_ID"
,"CommitIdOrigenPersona"
,"CommitIdDestinoPersona"
,"IdPersona"
,"IdCliente"
,"IdEstado"
,"DocumentoIdentidad" -- por tipo rut
,"IdTipoDocumentoIdentidad"
,"Nombres"
,"ApellidoPaterno"
,"ApellidoMaterno"
,"RazonSocial"
,"Giro"
,"PaisNacimiento"
,"PaisResidencia"
,"FechaNacimiento"
,"FechaDefuncion"
,"Sexo"
,"EsExtranjero"
,"EstadoCivil"
,"RegimenConyugal"
,"Cargo"
,"Direccion"
,"Calle"
,"NumeroDirf"
,"Departamento"
,"NumeroTelefono"
,"Prefesion"
 ) AS

 select
  HISPER."COMMIT_TIME"
, HISPER."COMMIT_ID"
,per."$validfrom$"
,per."$validto$"
,per."IdPersona"
,cli."IdCliente"
,per."IdEstado"
,doc."DocumentoIdentidad" -- por tipo rut
,doc."IdTipoDocumentoIdentidad"
,per."Nombres"
,per."ApellidoPaterno"
,per."ApellidoMaterno"
,per."RazonSocial"
,Giro."Nombre"
,PaisNac."NombrePais"
,PaisRes."NombrePais"
,per."FechaNacimiento"
,per."FechaDefuncion"
,per."Sexo"
,per."EsExtranjero"
,per."EstadoCivil"
,per."RegimenConyugal"
,per."Cargo"
,dire."Direccion"
,dirf."Calle"
,dirf."Numero"
,dirf."Departamento"
,tel."Numero"
,Prof."Nombre"

FROM
        "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"                   ('REQUEST_FLAGS'='ALLROWS') as per
        inner join SYS.TRANSACTION_HISTORY as HISPER on (HISPER."COMMIT_ID" = per."$validfrom$")

        left join "BSCL"."BSCL.Plataforma.BD::REG.Giro"  as Giro on (per."IdGiroPrincipal" = Giro."IdGiro")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Pais"  as PaisNac on (per."IdPaisNacimiento" = PaisNac."IdPais")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Pais"  as PaisRes on (per."IdPaisResidencia" = PaisRes."IdPais")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Profesion"  as Prof on (per."IdProfesion" = Prof."IdProfesion")
        ---------------- CLIENTE
        left join ( select HISCLI.COMMIT_TIME ,HISCLI.COMMIT_ID ,cli."IdCliente",cli."IdCategoriaInversionista",cli."IdPersona"
                    from "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" ('REQUEST_FLAGS'='ALLROWS') as Cli
                                  inner join SYS.TRANSACTION_HISTORY as HISCLI on (HISCLI."COMMIT_ID" = cli."$validfrom$")
                  ) as cli
                on (per."IdPersona" = cli."IdPersona" and cli."COMMIT_TIME" <= HISPER."COMMIT_TIME" )

        ---------------- Documento Identidad
        left join ( select HISDOC.COMMIT_TIME ,HISDOC.COMMIT_ID ,doc."DocumentoIdentidad" ,doc."IdTipoDocumentoIdentidad",doc."NumeroSerie",  doc."IdPersona"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" ('REQUEST_FLAGS'='ALLROWS') as doc
                                  inner join SYS.TRANSACTION_HISTORY as HISDOC on (HISDOC."COMMIT_ID" = doc."$validfrom$")
                  ) as doc
                on (per."IdPersona" = doc."IdPersona" and doc."IdTipoDocumentoIdentidad" = 'R' and doc."COMMIT_TIME" <= HISPER."COMMIT_TIME" )

        ---------------- Direccion Electronica
        left join ( select HISDIRE.COMMIT_TIME ,HISDIRE.COMMIT_ID ,dire."IdDireccionElectronica",dire."IdPersona" ,dire."Direccion"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as dire
                                  inner join SYS.TRANSACTION_HISTORY as HISDIRE on (HISDIRE."COMMIT_ID" = dire."$validfrom$")
                  ) as dire
                on (per."IdDireccionElectronicaPrincipal" = dire."IdDireccionElectronica" and per."IdPersona" = dire."IdPersona" and dire."COMMIT_TIME" <= HISPER."COMMIT_TIME")

        ---------------- Direccion Fisica
        left join ( select HISDIRF.COMMIT_TIME ,HISDIRF.COMMIT_ID ,dirf."IdDireccionFisica",dirf."IdPersona", dirf."Calle", dirf."Numero",dirf."Departamento"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as dirf
                                  inner join SYS.TRANSACTION_HISTORY as HISDIRF on (HISDIRF."COMMIT_ID" = dirf."$validfrom$")
                  ) as dirf
                on (per."IdDireccionFisicaPrincipal" = dirf."IdDireccionFisica" and per."IdPersona" = dirf."IdPersona" and dirf."COMMIT_TIME" <= HISPER."COMMIT_TIME")

        ---------------- Telefono
        left join ( select HISTEL.COMMIT_TIME ,HISTEL.COMMIT_ID ,tel."IdTelefono", tel."IdPersona", tel."Numero"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as tel
                                  inner join SYS.TRANSACTION_HISTORY as HISTEL on (HISTEL."COMMIT_ID" = tel."$validfrom$")
                  ) as tel
                on (per."IdTelefonoPrincipal" = tel."IdTelefono" and per."IdPersona" = tel."IdPersona" and tel."COMMIT_TIME" <= HISPER."COMMIT_TIME")
--WHERE per."IdPersona"  = 10000
order by per."IdPersona", HISPER."COMMIT_ID"
WITH READ ONLY;

GRANT SELECT ON "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

select TO_DATE("COMMIT_TIME") as "fecha", *  from "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente" WHERE "IdPersona" = 10000

----------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------- PRUEBAS ---------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------

update  "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY"  set "IdCategoriaInversionista" = 'NO CALIFICADO' WHERE "IdCliente" = 10000
COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY"  MERGE DELTA INDEX;

update "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY" set "Nombres" = 'Katherine Denise' WHERE "IdPersona" = 10000;
COMMIT;
UPDATE "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"  MERGE DELTA INDEX;


--- No afecta al registro histórico
--Alter table "BSCL.SocioNegocio.BD::PER.PersonaHISTORY" add ("ZEXZO" NVARCHAR(3) DEFAULT 'FEM');

-- Observaciones --
-------------------
-- invalid datatype: cannot allow BLOB type with non NULL threshold for temporary and history table column: FotoPersona: line 26 col 2 (at pos 1268)
-- depende del TRANSACTION_HISTORY, es una vista
-- Could not execute 'delete from SYS.TRANSACTION_HISTORY where COMMIT_ID = 35382603' SAP DBTech JDBC: [361]: data manipulation operation not legal on this view
-- Obliga a ejecutar sentencia UPDATE "BSCL.SocioNegocio.BD::PER.PersonaHISTORY" MERGE DELTA INDEX; sobre la tabla para incorporar los COMMIT_ID
-- que me llevan a la fecha de cambios

-------------------------------------------------------- VISTA CLIENTES CON HISTORY
SET SCHEMA "BSCL";

DROP VIEW "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente";

CREATE VIEW "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente"(
 "COMMIT_TIME"
,"COMMIT_ID"
,"CommitIdOrigenPersona"
,"CommitIdDestinoPersona"
,"IdPersona"
,"IdCliente"
,"IdEstado"
,"DocumentoIdentidad" -- por tipo rut
,"IdTipoDocumentoIdentidad"
,"Nombres"
,"ApellidoPaterno"
,"ApellidoMaterno"
,"RazonSocial"
,"Giro"
,"PaisNacimiento"
,"PaisResidencia"
,"FechaNacimiento"
,"FechaDefuncion"
,"Sexo"
,"EsExtranjero"
,"EstadoCivil"
,"RegimenConyugal"
,"Cargo"
,"Direccion"
,"Calle"
,"NumeroDirf"
,"Departamento"
,"NumeroTelefono"
,"Prefesion"
 ) AS

 select
  HISPER."COMMIT_TIME"
, HISPER."COMMIT_ID"
,per."$validfrom$"
,per."$validto$"
,per."IdPersona"
,cli."IdCliente"
,per."IdEstado"
,doc."DocumentoIdentidad" -- por tipo rut
,doc."IdTipoDocumentoIdentidad"
,per."Nombres"
,per."ApellidoPaterno"
,per."ApellidoMaterno"
,per."RazonSocial"
,Giro."Nombre"
,PaisNac."NombrePais"
,PaisRes."NombrePais"
,per."FechaNacimiento"
,per."FechaDefuncion"
,per."Sexo"
,per."EsExtranjero"
,per."EstadoCivil"
,per."RegimenConyugal"
,per."Cargo"
,dire."Direccion"
,dirf."Calle"
,dirf."Numero"
,dirf."Departamento"
,tel."Numero"
,Prof."Nombre"

FROM
        "BSCL"."BSCL.SocioNegocio.BD::PER.PersonaHISTORY"                   ('REQUEST_FLAGS'='ALLROWS') as per
        inner join SYS.TRANSACTION_HISTORY as HISPER on (HISPER."COMMIT_ID" = per."$validfrom$")

        left join "BSCL"."BSCL.Plataforma.BD::REG.Giro"  as Giro on (per."IdGiroPrincipal" = Giro."IdGiro")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Pais"  as PaisNac on (per."IdPaisNacimiento" = PaisNac."IdPais")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Pais"  as PaisRes on (per."IdPaisResidencia" = PaisRes."IdPais")
        left join "BSCL"."BSCL.Plataforma.BD::REG.Profesion"  as Prof on (per."IdProfesion" = Prof."IdProfesion")
        ---------------- CLIENTE
        left join ( select HISCLI.COMMIT_TIME ,HISCLI.COMMIT_ID ,cli."IdCliente",cli."IdCategoriaInversionista",cli."IdPersona"
                    from "BSCL"."BSCL.SocioNegocio.BD::CLI.ClienteHISTORY" ('REQUEST_FLAGS'='ALLROWS') as Cli
                                  inner join SYS.TRANSACTION_HISTORY as HISCLI on (HISCLI."COMMIT_ID" = cli."$validfrom$")
                  ) as cli
                on (per."IdPersona" = cli."IdPersona" and cli."COMMIT_TIME" <= HISPER."COMMIT_TIME" )

        ---------------- Documento Identidad
        left join ( select HISDOC.COMMIT_TIME ,HISDOC.COMMIT_ID ,doc."DocumentoIdentidad" ,doc."IdTipoDocumentoIdentidad",doc."NumeroSerie",  doc."IdPersona"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidadHISTORY" ('REQUEST_FLAGS'='ALLROWS') as doc
                                  inner join SYS.TRANSACTION_HISTORY as HISDOC on (HISDOC."COMMIT_ID" = doc."$validfrom$")
                  ) as doc
                on (per."IdPersona" = doc."IdPersona" and doc."IdTipoDocumentoIdentidad" = 'R' and doc."COMMIT_TIME" <= HISPER."COMMIT_TIME" )

        ---------------- Direccion Electronica
        left join ( select HISDIRE.COMMIT_TIME ,HISDIRE.COMMIT_ID ,dire."IdDireccionElectronica",dire."IdPersona" ,dire."Direccion"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronicaHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as dire
                                  inner join SYS.TRANSACTION_HISTORY as HISDIRE on (HISDIRE."COMMIT_ID" = dire."$validfrom$")
                  ) as dire
                on (per."IdDireccionElectronicaPrincipal" = dire."IdDireccionElectronica" and per."IdPersona" = dire."IdPersona" and dire."COMMIT_TIME" <= HISPER."COMMIT_TIME")

        ---------------- Direccion Fisica
        left join ( select HISDIRF.COMMIT_TIME ,HISDIRF.COMMIT_ID ,dirf."IdDireccionFisica",dirf."IdPersona", dirf."Calle", dirf."Numero",dirf."Departamento"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisicaHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as dirf
                                  inner join SYS.TRANSACTION_HISTORY as HISDIRF on (HISDIRF."COMMIT_ID" = dirf."$validfrom$")
                  ) as dirf
                on (per."IdDireccionFisicaPrincipal" = dirf."IdDireccionFisica" and per."IdPersona" = dirf."IdPersona" and dirf."COMMIT_TIME" <= HISPER."COMMIT_TIME")

        ---------------- Telefono
        left join ( select HISTEL.COMMIT_TIME ,HISTEL.COMMIT_ID ,tel."IdTelefono", tel."IdPersona", tel."Numero"
                    from "BSCL"."BSCL.SocioNegocio.BD::PER.TelefonoHISTORY"  ('REQUEST_FLAGS'='ALLROWS') as tel
                                  inner join SYS.TRANSACTION_HISTORY as HISTEL on (HISTEL."COMMIT_ID" = tel."$validfrom$")
                  ) as tel
                on (per."IdTelefonoPrincipal" = tel."IdTelefono" and per."IdPersona" = tel."IdPersona" and tel."COMMIT_TIME" <= HISPER."COMMIT_TIME")
--WHERE per."IdPersona"  = 10000
order by per."IdPersona", HISPER."COMMIT_ID"
WITH READ ONLY;

GRANT SELECT ON "BSCL"."BSCL.Normativa.BD::NOR.ConsultarCliente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

