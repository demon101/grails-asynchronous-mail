package grails.plugin.asyncmail


import grails.plugins.mail.MailMessageContentRender
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static grails.plugin.asyncmail.enums.MessageStatus.CREATED
/**
 * @author Vitalii Samolovskikh aka Kefir, Puneet Behl
 */
@TestMixin(GrailsUnitTestMixin)
@Mock(AsynchronousMailMessage)
@ConfineMetaClassChanges(AsynchronousMailMessageBuilder)
class AsynchronousMailMessageBuilderSpec extends Specification {

    AsynchronousMailMessageBuilderFactory asynchronousMailMessageBuilderFactory

    void setup() {
        asynchronousMailMessageBuilderFactory = new AsynchronousMailMessageBuilderFactory()
        asynchronousMailMessageBuilderFactory.configuration = grailsApplication.config
    }

    @Unroll("testing builder for params immediate = #immediateVal and delete = #deleteVal")
    void "testing builder"() {
        setup:
        def c = {
            from 'John Smith <john@example.com>'
            to 'test1@example.com'
            envelopeFrom 'mary@example.com'
            subject 'Subject'
            text 'Text'
            immediate immediateVal
            delete deleteVal
            priority 1
        }
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message
        message.validate()

        then:
        message.from == 'John Smith <john@example.com>'
        message.to == ['test1@example.com']
        message.envelopeFrom == 'mary@example.com'
        message.subject == 'Subject'
        message.text == 'Text'
        message.status == CREATED
        message.markDelete == markDeleteVal
        message.markDeleteAttachments == markDeleteAttachmentsVal

        message.priority == 1
        builder.immediatelySetted == immediatelySettedVal
        builder.immediately == immediatelyVal

        where:

        immediateVal | deleteVal     | immediatelySettedVal | immediatelyVal | markDeleteVal | markDeleteAttachmentsVal
        true         | true          |  true                | true           | true          | false
        false        | 'attachments' |  true                | false          | false         | true
        true         | false         |  true                | true           | false         | false
    }

    void "testing mail with minimum data"() {
        setup:
        def c = {
            to 'john@example.com'
            subject 'Subject'
            text 'Text'
        }
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message

        then:
        message.validate()
    }

    void testBodyHtmlRender(){
        setup:
        overrideDoRenderMethod('text/html')
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message
        def c = {
            to 'test@example.com'
            subject 'Subject'
            body view:'/test/html'
        }

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message
        message.validate()

        then:
        message.to == ['test@example.com']
        message.subject == 'Subject'
        message.html
        message.text
    }

    void testHtmlRender(){
        setup:
        overrideDoRenderMethod('text/html')
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message
        def c = {
            to 'test@example.com'
            subject 'Subject'
            locale Locale.ENGLISH
            html view:'/test/html'
        }

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message
        message.validate()

        then:
        message.to == ['test@example.com']
        message.subject == 'Subject'
        message.html
        message.text
    }

    void testTextRender(){
        setup:
        overrideDoRenderMethod('text/plain')
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message
        def c = {
            to 'test@example.com'
            subject 'Subject'
            locale 'en_us'
            text view:'/test/plain'
        }

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message
        message.validate()

        then:
        message.to == ['test@example.com']
        message.subject == 'Subject'
        !message.html
        message.text
    }

    void testBodyTextRender(){
        setup:
        overrideDoRenderMethod('text/plain')
        AsynchronousMailMessageBuilder builder
        AsynchronousMailMessage message
        def c = {
            to 'test@example.com'
            subject 'Subject'
            body view:'/test/plain'
        }

        when:
        builder = asynchronousMailMessageBuilderFactory.createBuilder()
        c.delegate = builder
        c.call()
        message = builder.message
        message.validate()

        then:
        message.to == ['test@example.com']
        message.subject == 'Subject'
        !message.html
        message.text
    }

    void testMultipartAlternative(){
        setup:
            AsynchronousMailMessageBuilder builder
            AsynchronousMailMessage message
            def c = {
                to 'test@example.com'
                subject 'Subject'
                html '<html>HTML text</html>'
                text 'Plain text'
            }

        when:
            builder = asynchronousMailMessageBuilderFactory.createBuilder()
            c.delegate = builder
            c.call()
            message = builder.message
            message.validate()

        then:
            message.to == ['test@example.com']
            message.subject == 'Subject'
            message.html
            message.text
            message.alternative
    }

    protected void overrideDoRenderMethod(String contentType) {
        AsynchronousMailMessageBuilder.metaClass.doRender = {Map params->
            new MailMessageContentRender(null, contentType)
        }
    }
}
