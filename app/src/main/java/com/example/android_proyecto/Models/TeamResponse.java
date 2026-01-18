package com.example.android_proyecto.Models;

import java.util.List;

public class TeamResponse {

    private String team;
    private List<TeamMember> members;

    public TeamResponse() {
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }
}
