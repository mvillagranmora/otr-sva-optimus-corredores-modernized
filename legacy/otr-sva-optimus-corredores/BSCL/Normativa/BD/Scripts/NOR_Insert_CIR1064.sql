SET SCHEMA "BSCL";

DROP TABLE #v_IdProgramTable;

CREATE LOCAL TEMPORARY TABLE #v_IdProgramTable
(
  "NombreParametro"   NVARCHAR(100),      
   "Valor"   NVARCHAR(100)
);

INSERT INTO #v_IdProgramTable VALUES ('IdEmpresa','1');
INSERT INTO #v_IdProgramTable VALUES ('schema','BSCL');
INSERT INTO #v_IdProgramTable VALUES ('grabar','false');
INSERT INTO #v_IdProgramTable VALUES ('reemplazar','false');
INSERT INTO #v_IdProgramTable VALUES ('separadorListas',';');
INSERT INTO #v_IdProgramTable VALUES ('simboloDecimal',',');
INSERT INTO #v_IdProgramTable VALUES ('simboloFecha','dd/MM/yyyy');
INSERT INTO #v_IdProgramTable VALUES ('aceptarFechasNull','true');
INSERT INTO #v_IdProgramTable VALUES ('EsPauta','S');
INSERT INTO #v_IdProgramTable VALUES ('IdEmpresa','1');
INSERT INTO #v_IdProgramTable VALUES ('idUsuario','14');
INSERT INTO #v_IdProgramTable VALUES ('idDocumento','1007');

call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda" (1,1,'NOR','GeneraCIR1064','N','**','*','**',8,30,'S','N','N','S',#v_IdProgramTable,?,?,?,?,?,?);

------------------------------------

DROP TABLE #v_IdProgramTableRUEDA;

CREATE LOCAL TEMPORARY TABLE #v_IdProgramTableRUEDA
(
  "NombreParametro"   NVARCHAR(100),      
   "Valor"   NVARCHAR(100)
);

INSERT INTO #v_IdProgramTableRUEDA VALUES ('IdEmpresa','1');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('schema','BSCL');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('grabar','false');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('reemplazar','false');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('separadorListas',';');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('simboloDecimal',',');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('simboloFecha','dd/MM/yyyy');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('aceptarFechasNull','true');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('EsPauta','S');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('IdEmpresa','1');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('idUsuario','14');
INSERT INTO #v_IdProgramTableRUEDA VALUES ('idDocumento','1013');

call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda" (1,1,'NOR','GeneraCIR1064R','N','**','*','**',8,30,'S','N','N','S',#v_IdProgramTableRUEDA,?,?,?,?,?,?);

