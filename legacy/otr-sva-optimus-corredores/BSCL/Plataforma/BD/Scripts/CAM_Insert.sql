SET SCHEMA "BSCL";
-- Para CHILE se refiere a las camaras del CCLV

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::CAM.Camara"; 
DELETE FROM  "BSCL.Plataforma.BD::CAM.GrupoCamara"; 


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::CAM.Camara                                               */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::CAM.Camara"("IdCamara","Nombre","CodigoContraparte","IdEmpresa","IdEstado")
VALUES ('RV','RENTA VARIABLE','CCLV',1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.Camara"("IdCamara","Nombre","CodigoContraparte","IdEmpresa","IdEstado")
VALUES ('PM','PAGO MAÑANA','CCLV',1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.Camara"("IdCamara","Nombre","CodigoContraparte","IdEmpresa","IdEstado")
VALUES ('PH','PAGO HOY','CCLV',1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.Camara"("IdCamara","Nombre","CodigoContraparte","IdEmpresa","IdEstado")
VALUES ('SM','SIMULTANEA','CCLV',1,'ACT');



/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::CAM.GrupoCamara                                               */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::CAM.GrupoCamara"("IdGrupoCamara","SecuenciaAgrupacion","GrupoCamara","CodigoGrupo","IdCamara","IdEstado")
VALUES (1,'S','RV',3,'RV','ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.GrupoCamara"("IdGrupoCamara","SecuenciaAgrupacion","GrupoCamara","CodigoGrupo","IdCamara","IdEstado")
VALUES (2,'S','PM',1,'PM','ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.GrupoCamara"("IdGrupoCamara","SecuenciaAgrupacion","GrupoCamara","CodigoGrupo","IdCamara","IdEstado")
VALUES (3,'S','PH + SM',2,'PH;SM','ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.GrupoCamara"("IdGrupoCamara","SecuenciaAgrupacion","GrupoCamara","CodigoGrupo","IdCamara","IdEstado")
VALUES (4,'N','RV + PH',1,'RV;PH','ACT');

INSERT INTO "BSCL.Plataforma.BD::CAM.GrupoCamara"("IdGrupoCamara","SecuenciaAgrupacion","GrupoCamara","CodigoGrupo","IdCamara","IdEstado")
VALUES (5,'N','PH + SM',2,'PH;SM','ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
