select userid as "Id",user_code as "Codigo",u_name as "Nombre",CASE superuser WHEN 'Y' THEN 'Y' ELSE 'N' END as "Superusuario",coalesce("lastLogin",'0001-01-01') as "UltimoLogin",
coalesce(users."createDate",'0001-01-01') as "FechaCreacion","Locked" as "Bloqueado","PwdExp" as "DiasExpiracion",coalesce(string_agg(g."GroupName"),'') as "Grupos",
U_BCS_COBROCUENTA 
from ousr users
left join usr7 ug on users.userid = ug."UserId"
left join ougr g on g."GroupId" = ug."GroupId" ,oppa
where GROUPS = 0
group by userid,user_code,u_name,superuser,"lastLogin",users."createDate","Locked","PwdExp",U_BCS_COBROCUENTA
ORDER BY "Id"

--select * from sura.usr7
--select * from sura.OUGR
--select * from sura.oppa



SELECT * FROM oprm

SELECT * FROM USR3 --PERMISOS PERSONAS

SELECT * FROM UGR1 --PERMISO GRUPO

SELECT * FROM OUPT
