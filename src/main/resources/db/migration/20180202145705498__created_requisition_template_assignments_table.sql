CREATE TABLE requisition_template_assignments (
    id UUID PRIMARY KEY NOT NULL,
    programid uuid NOT NULL,
    facilitytypeid uuid,
    templateid uuid NOT NULL
);

ALTER TABLE ONLY requisition_template_assignments
    ADD CONSTRAINT requisition_template_assignment_unique_program_facility_type_template UNIQUE (facilitytypeid, programid, templateid);