SET SCHEMA "BSCL";

-- Script para modelo Peru. DivisionPais representa Distritos.
-- Clasificacion Division Pais, se insertan los Departamentos de Peru.


/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" where "IdPais" = 166;
DELETE FROM "BSCL.Plataforma.BD::REG.DivisionPais"  where "IdPais" = 166;


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

--ClasificacionDivisionPais PERU : Departamentos
	--DivisionPais PERU : Distritos

INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (16,'Amazonas','DEPARTAMENTO',166);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(347,166,0,'ARAMANGO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(348,166,0,'COPALLIN',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(349,166,0,'EL PARCO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(350,166,0,'IMAZA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(351,166,0,'LA PECA',16);	
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(352,166,0,'CHISQUILLA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(353,166,0,'CHURUJA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(354,166,0,'COROSHA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(355,166,0,'CUISPES',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(356,166,0,'FLORIDA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(357,166,0,'JAZAN',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(358,166,0,'JUMBILLA',16);	
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(359,166,0,'RECTA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(360,166,0,'SAN CARLOS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(361,166,0,'SHIPASBAMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(362,166,0,'VALERA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(363,166,0,'YAMBRASBAMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(364,166,0,'ASUNCION',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(365,166,0,'BALSAS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(366,166,0,'CHACHAPOYAS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(367,166,0,'CHETO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(368,166,0,'CHILIQUIN',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(369,166,0,'CHUQUIBAMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(370,166,0,'GRANADA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(371,166,0,'HUANCAS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(372,166,0,'LA JALCA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(373,166,0,'LEVANTO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(374,166,0,'LEYMEBAMBA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(375,166,0,'MAGDALENA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(376,166,0,'MARISCAL CASTILLA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(377,166,0,'MOLINOPAMPA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(378,166,0,'MONTEVIDEO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(379,166,0,'OLLEROS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(380,166,0,'QUINJALCA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(381,166,0,'SAN FRANCISCO DE DAGUAS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(382,166,0,'SAN ISIDRO DE MAINO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(383,166,0,'SOLOCO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(384,166,0,'SONCHE ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(385,166,0,'EL CENEPA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(386,166,0,'NIEVA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(387,166,0,'RIO SANTIAGO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(388,166,0,'CAMPORREDONDO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(389,166,0,'COCABAMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(390,166,0,'COLCAMAR ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(391,166,0,'CONILA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(392,166,0,'INGUILPATA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(393,166,0,'LAMUD',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(394,166,0,'LONGUITA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(395,166,0,'LONYA CHICO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(396,166,0,'LUYA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(397,166,0,'LUYA VIEJO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(398,166,0,'MARÍA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(399,166,0,'OCALLI ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(400,166,0,'OCUMAL ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(401,166,0,'PISUQUIA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(402,166,0,'PROVIDENCIA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(403,166,0,'SAN CRISTÓBAL',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(404,166,0,'SAN FRANCISCO DEL YESO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(405,166,0,'SAN JERÓNIMO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(406,166,0,'SAN JUAN DE LOPECANCHA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(407,166,0,'SANTA CATALINA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(408,166,0,'SANTO TOMÁS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(409,166,0,'TINGO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(410,166,0,'TRITA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(411,166,0,'CHIRIMOTO',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(412,166,0,'COCHAMAL ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(413,166,0,'HUAMBO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(414,166,0,'LIMABAMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(415,166,0,'LONGAR ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(416,166,0,'MARISCAL BENAVIDES ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(417,166,0,'MILPUC ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(418,166,0,'OMIA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(419,166,0,'SAN NICOLÁS',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(420,166,0,'SANTA ROSA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(421,166,0,'TOTORA ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(422,166,0,'VISTA ALEGRE ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(423,166,0,'BAGUA GRANDE ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(424,166,0,'CAJARURO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(425,166,0,'CUMBA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(426,166,0,'EL MILAGRO ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(427,166,0,'JAMALCA',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(428,166,0,'LONYA GRANDE ',16);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(429,166,0,'YAMON',16);	
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (17,'Ancash','DEPARTAMENTO',166);

INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (18,'Apurimac','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (19,'Arequipa','DEPARTAMENTO',166);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(430,166,171,'ALTO SELVA ALEGRE',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(431,166,171,'AREQUIPA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(432,166,171,'CAYMA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(433,166,171,'CERRO COLORADO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(434,166,171,'CHARACATO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(435,166,171,'CHIGUATA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(436,166,171,'JACOBO HUNTER ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(437,166,171,'JOSE LUIS BUSTAMANTE Y RIVERO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(438,166,171,'LA JOYA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(439,166,171,'MARIANO MELGAR',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(440,166,171,'MIRAFLORES',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(441,166,171,'MOLLEBAYA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(442,166,171,'PAUCARPATA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(443,166,171,'POCSI ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(444,166,171,'POLOBAYA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(445,166,171,'QUEQUEÑA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(446,166,171,'SABANDIA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(447,166,171,'SACHACA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(448,166,171,'SAN JUAN DE SIGUAS',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(449,166,171,'SAN JUAN DE TARUCANI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(450,166,171,'SANTA ISABEL DE SIGUAS',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(451,166,171,'SANTA RITA DE SIGUAS',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(452,166,171,'SOCABAYA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(453,166,171,'TIABAYA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(454,166,171,'UCHUMAYO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(455,166,171,'VITOR ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(456,166,171,'YANAHUARA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(457,166,171,'YARABAMBA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(458,166,171,'YURA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(459,166,171,'CAMANÁ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(460,166,171,'JOSE MARIA QUIMPER',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(461,166,171,'MARIANO NICOLAS VALCARCEL ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(462,166,171,'MARISCAL CACERES',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(463,166,171,'NICOLÁS DE PIEROLA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(464,166,171,'OCOÑA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(465,166,171,'QUILCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(466,166,171,'SAMUEL PASTOR ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(467,166,171,'ACARÍ ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(468,166,171,'ATICO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(469,166,171,'ATIQUIPA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(470,166,171,'BELLA UNIÓN ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(471,166,171,'CAHUACHO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(472,166,171,'CARAVELI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(473,166,171,'CHALA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(474,166,171,'CHAPARRA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(475,166,171,'HUANUHUANU',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(476,166,171,'JAQUI ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(477,166,171,'LOMAS ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(478,166,171,'QUICACHA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(479,166,171,'YAUCA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(480,166,171,'ANDAGUA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(481,166,171,'APLAO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(482,166,171,'AYO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(483,166,171,'CHACHAS ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(484,166,171,'CHILCAYMARCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(485,166,171,'CHOCO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(486,166,171,'HUANCARQUI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(487,166,171,'MACHAGUAY ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(488,166,171,'ORCOPAMPA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(489,166,171,'PAMPACOLCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(490,166,171,'TIPAN ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(491,166,171,'UÑON',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(492,166,171,'URACA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(493,166,171,'VIRACO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(494,166,171,'ACHOMA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(495,166,171,'CABANACONDE ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(496,166,171,'CALLALLI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(497,166,171,'CAYLLOMA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(498,166,171,'CHIVAY',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(499,166,171,'COPORAQUE ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(500,166,171,'HUAMBO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(501,166,171,'HUANCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(502,166,171,'ICHUPAMPA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(503,166,171,'LARI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(504,166,171,'LLUTA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(505,166,171,'MACA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(506,166,171,'MADRIGAL',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(507,166,171,'MAJES ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(508,166,171,'SAN ANTONIO DE CHUCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(509,166,171,'SIBAYO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(510,166,171,'TAPAY ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(511,166,171,'TISCO ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(512,166,171,'TUTI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(513,166,171,'YANQUE',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(514,166,171,'ANDARAY ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(515,166,171,'CAYARANI',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(516,166,171,'CHICHAS ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(517,166,171,'CHUQUIBAMBA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(518,166,171,'IRAY',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(519,166,171,'RIO GRANDE',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(520,166,171,'SALAMANCA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(521,166,171,'YANAQUIHUA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(522,166,171,'COCACHACRA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(523,166,171,'DEAN VALDIVIA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(524,166,171,'ISLAY ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(525,166,171,'MEJIA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(526,166,171,'MOLLENDO',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(527,166,171,'PUNTA DE BOMBON ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(528,166,171,'ALCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(529,166,171,'CHARCANA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(530,166,171,'COTAHUASI ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(531,166,171,'HUAYNACOTAS ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(532,166,171,'PAMPAMARCA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(533,166,171,'PUYCA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(534,166,171,'QUECHUALLA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(535,166,171,'SAYLA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(536,166,171,'TAURIA',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(537,166,171,'TOMEPAMPA ',19);
	INSERT INTO "BSCL.Plataforma.BD::REG.DivisionPais" ("IdDivisionPais","IdPais","IdCiudad","NombreDivisionPais","IdClasificacion1") VALUES(538,166,171,'TORO',19);

INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (20,'Ayacucho','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (21,'Cajamarca','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (22,'Callao','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (23,'Cusco','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (24,'Huancavelica','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (25,'Huanuco','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (26,'Ica','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (27,'Junin','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (28,'La Libertad','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (29,'Lambayeque','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (30,'Lima Metropolitana','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (31,'Lima (Region)','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (32,'Loreto','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (33,'Madre de Dios','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (34,'Moquegua','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (35,'Pasco','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (36,'Piura','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (37,'Puno','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (38,'San Martin','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (39,'Tacna','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (40,'Tumbes','DEPARTAMENTO',166);
INSERT INTO  "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" ("IdClasificacionDivisionPais","NombreClasificacionDivisionPais","TipoClasificacionDivisionPais","IdPais") VALUES (41,'Ucayali','DEPARTAMENTO',166);

	


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
