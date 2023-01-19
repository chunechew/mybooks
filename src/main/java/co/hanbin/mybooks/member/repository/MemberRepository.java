package co.hanbin.mybooks.member.repository;

import org.springframework.data.repository.CrudRepository;

import co.hanbin.mybooks.member.entity.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {
    Member findByUsername(String username);
}
