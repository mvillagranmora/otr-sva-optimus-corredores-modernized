select distinct user_code,u_name from (
select 'sboagf' as company,user_Code,u_name,"lastLogin" from sboagf.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sbobcp' as company,user_Code,u_name,"lastLogin" from sbobcp.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sbofipb' as company,user_Code,u_name,"lastLogin" from sbofipb.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtadminv' as company,user_Code,u_name,"lastLogin" from sboimtadminv.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtasfin' as company,user_Code,u_name,"lastLogin" from sboimtasfin.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtcorr' as company,user_Code,u_name,"lastLogin" from sboimtcorr.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimthold' as company,user_Code,u_name,"lastLogin" from sboimthold.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtint' as company,user_Code,u_name,"lastLogin" from sboimtint.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtinv' as company,user_Code,u_name,"lastLogin" from sboimtinv.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtperu' as company,user_Code,u_name,"lastLogin" from sboimtperu.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtsecur' as company,user_Code,u_name,"lastLogin" from sboimtsecur.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
)
where "lastLogin" >= '2021-06-01'


select COMPANY ,user_code,u_name from (
select 'sboagf' as company,user_Code,u_name,"lastLogin" from sboagf.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sbobcp' as company,user_Code,u_name,"lastLogin" from sbobcp.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sbofipb' as company,user_Code,u_name,"lastLogin" from sbofipb.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtadminv' as company,user_Code,u_name,"lastLogin" from sboimtadminv.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtasfin' as company,user_Code,u_name,"lastLogin" from sboimtasfin.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtcorr' as company,user_Code,u_name,"lastLogin" from sboimtcorr.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimthold' as company,user_Code,u_name,"lastLogin" from sboimthold.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtint' as company,user_Code,u_name,"lastLogin" from sboimtint.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtinv' as company,user_Code,u_name,"lastLogin" from sboimtinv.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtperu' as company,user_Code,u_name,"lastLogin" from sboimtperu.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
union all
select 'sboimtsecur' as company,user_Code,u_name,"lastLogin" from sboimtsecur.ousr where superuser = 'N' and user_code not like '%manag%' and user_code not like '%bscl%' and user_code not like '%scl%'
)
where user_code in ('amunozd','crojas','fhermosi','lperalta','cvazques','lsanabri','dmancipe')
order by COMPANY 





