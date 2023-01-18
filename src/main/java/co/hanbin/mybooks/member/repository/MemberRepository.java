package co.hanbin.mybooks.member.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.hanbin.mybooks.member.entity.Member;

@Repository
public interface MemberRepository extends CrudRepository<Member, Long> {
    Member findByUsername(String username);
}
