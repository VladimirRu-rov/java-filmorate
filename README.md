# java-filmorate

# Промежуточное задание в спринте №11.

![Схема базы данных для этого проекта](DB_scheme.png "Тут доложен был быть анекдот")

`1. Найти все фильмы, которые лайкнул пользователь 1.`   
**SELECT**  
f.id,  
f.name,  
f.release_date  
**FROM** film f
**JOIN** like_list ll ON f.id = ll.film_id  
**WHERE** ll.user_id = 1  
**ORDER BY** f.release_date DESC;

`2. Подсчитать, сколько пользователей лайкнуло фильм 200.`  
**SELECT**  
COUNT(user_id) AS likes_count  
**FROM** like_list  
**WHERE** film_id = 200;

`3.Найти пользователей, у которых день рождения в декабре.`  
**SELECT**   
id,  
name,  
email,  
birthday  
**FROM** user  
**WHERE** MONTH(birthday) = 12  
**ORDER BY** DAY(birthday);  